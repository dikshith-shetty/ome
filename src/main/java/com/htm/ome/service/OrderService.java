package com.htm.ome.service;

import com.htm.ome.dto.OrderRequest;
import com.htm.ome.dto.OrderResponse;
import com.htm.ome.dto.Trade;
import com.htm.ome.model.Order;
import com.htm.ome.model.TradeModel;
import com.htm.ome.enums.OrderStatus;
import com.htm.ome.core.MatchEngine;
import com.htm.ome.store.InMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final InMemoryStore store;
    private final MatchEngine matchEngine;
    private final ExecutorService matchEngineExecutor;
    private final ConcurrentHashMap<String, ReentrantLock> assetLocks = new ConcurrentHashMap<>();

    public OrderResponse createOrder(OrderRequest req) {
        long id = store.nextOrderId();
        Order order = Order.builder()
                .id(id)
                .asset(req.getAsset())
                .price(req.getPrice())
                .amount(req.getAmount())
                .pendingAmount(req.getAmount())
                .direction(req.getDirection())
                .status(OrderStatus.OPEN)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .modifiedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        store.saveOrder(order);
        log.info("Order created id={} asset={} price={} amount={}",
                id, order.getAsset(), order.getPrice(), order.getAmount());

        List<TradeModel> tradeModels = processOrder(order);
        tradeModels.forEach(t -> {
            t.setId(store.nextTradeId());
            store.saveTrade(t);
        });
        log.info("Order processing completed for id={}", order.getId());

        List<Trade> trades = tradeModels.stream()
                .map(t -> Trade.builder()
                        .orderId(t.getBuyOrderId().equals(order.getId()) ? t.getSellOrderId() : t.getBuyOrderId())
                        .amount(t.getAmount())
                        .price(t.getPrice()).build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .timestamp(order.getCreatedAt())
                .asset(order.getAsset())
                .price(order.getPrice())
                .amount(order.getAmount())
                .pendingAmount(order.getPendingAmount())
                .direction(order.getDirection())
                .trades(trades).build();
    }

    private List<TradeModel> processOrder(Order order) {
        try {
            Future<List<TradeModel>> future = matchEngineExecutor.submit(() -> {
                ReentrantLock lock = assetLocks.computeIfAbsent(order.getAsset(), a -> new ReentrantLock());
                lock.lock();
                try {
                    List<TradeModel> trades = matchEngine.match(order);
                    matchEngine.addOrderToBook(order);
                    order.setModifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    return trades;
                } catch (Exception e) {
                    log.error("Error during matching for orderId={}", order.getId(), e);
                    throw e;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            });

            return future.get();
        } catch (Exception e) {
            log.error("Error during matching for orderId={}", order.getId(), e);
            throw new RuntimeException(e);
        }
    }

    public OrderResponse getOrder(Long id) {
        Order order = store.getOrder(id);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + id);
        }
        List<Trade> trades = store.allTrades()
                .stream()
                .filter(t -> t.getBuyOrderId().equals(id) || t.getSellOrderId().equals(id))
                .map(t -> Trade.builder()
                        .orderId(t.getBuyOrderId().equals(id) ? t.getSellOrderId() : t.getBuyOrderId())
                        .amount(t.getAmount())
                        .price(t.getPrice()).build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .timestamp(order.getCreatedAt())
                .asset(order.getAsset())
                .price(order.getPrice())
                .amount(order.getAmount())
                .pendingAmount(order.getPendingAmount())
                .direction(order.getDirection())
                .trades(trades)
                .build();
    }
}
