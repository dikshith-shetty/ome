package com.htm.ome.core;

import com.htm.ome.model.Order;
import com.htm.ome.model.TradeModel;
import com.htm.ome.enums.OrderDirection;
import com.htm.ome.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
@Slf4j
public class MatchEngine {

    private final Map<String, ConcurrentSkipListMap<Double, Deque<Order>>> buyBooks = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentSkipListMap<Double, Deque<Order>>> sellBooks = new ConcurrentHashMap<>();

    public List<TradeModel> match(Order incoming) {
        List<TradeModel> trades = new ArrayList<>();
        buyBooks.computeIfAbsent(incoming.getAsset(), k -> new ConcurrentSkipListMap<>(Comparator.reverseOrder()));
        sellBooks.computeIfAbsent(incoming.getAsset(), k -> new ConcurrentSkipListMap<>());

        if (incoming.getDirection() == OrderDirection.BUY) {
            matchBuy(incoming, trades);
        } else {
            matchSell(incoming, trades);
        }
        log.info("MatchEngine: completed matching for orderId={} asset={} pending={}",
                incoming.getId(), incoming.getAsset(), incoming.getPendingAmount());

        return trades;
    }

    private void matchBuy(Order buyOrder, List<TradeModel> trades) {
        ConcurrentSkipListMap<Double, Deque<Order>> asks = sellBooks.get(buyOrder.getAsset());
        while (!asks.isEmpty() && buyOrder.getPendingAmount() > 0) {
            Map.Entry<Double, Deque<Order>> best = asks.firstEntry();
            if (best == null) {
                break;
            }
            Double askPrice = best.getKey();
            if (askPrice > buyOrder.getPrice()) {
                break;
            }
            Deque<Order> q = best.getValue();
            while (!q.isEmpty() && buyOrder.getPendingAmount() > 0) {
                Order ask = q.peekFirst();
                double traded = Math.min(buyOrder.getPendingAmount(), ask.getPendingAmount());
                traded = Math.round(traded * 100.0) / 100.0;
                buyOrder.setPendingAmount(Math.round((buyOrder.getPendingAmount() - traded) * 100.0) / 100.0);
                ask.setPendingAmount(Math.round((ask.getPendingAmount() - traded) * 100.0) / 100.0);
                ask.setModifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
                updateStatus(buyOrder);
                updateStatus(ask);
                TradeModel t = TradeModel.builder()
                        .buyOrderId(buyOrder.getId())
                        .sellOrderId(ask.getId())
                        .price(askPrice)
                        .amount(traded)
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build();
                trades.add(t);
                log.info("Trade executed: buyId={} sellId={} asset={} price={} amount={}",
                        buyOrder.getId(), ask.getId(), buyOrder.getAsset(), askPrice, traded);

                if (ask.getPendingAmount() == 0) {
                    q.pollFirst();
                }
            }
            if (q.isEmpty()) {
                asks.remove(askPrice);
            }
        }
    }

    private void matchSell(Order sellOrder, List<TradeModel> trades) {
        ConcurrentSkipListMap<Double, Deque<Order>> bids = buyBooks.get(sellOrder.getAsset());
        while (!bids.isEmpty() && sellOrder.getPendingAmount() > 0) {
            Map.Entry<Double, Deque<Order>> best = bids.firstEntry();
            if (best == null) {
                break;
            }
            Double bidPrice = best.getKey();
            if (bidPrice < sellOrder.getPrice()) {
                break;
            }
            Deque<Order> q = best.getValue();
            while (!q.isEmpty() && sellOrder.getPendingAmount() > 0) {
                Order bid = q.peekFirst();
                double traded = Math.min(sellOrder.getPendingAmount(), bid.getPendingAmount());
                traded = Math.round(traded * 100.0) / 100.0;
                sellOrder.setPendingAmount(Math.round((sellOrder.getPendingAmount() - traded) * 100.0) / 100.0);
                bid.setPendingAmount(Math.round((bid.getPendingAmount() - traded) * 100.0) / 100.0);
                bid.setModifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
                updateStatus(sellOrder);
                updateStatus(bid);
                TradeModel t = TradeModel.builder()
                        .buyOrderId(bid.getId())
                        .sellOrderId(sellOrder.getId())
                        .price(bidPrice)
                        .amount(traded)
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build();
                trades.add(t);
                log.info("Trade executed: buyId={} sellId={} asset={} price={} amount={}",
                        bid.getId(), sellOrder.getId(), sellOrder.getAsset(), bidPrice, traded);

                if (bid.getPendingAmount() == 0) {
                    q.pollFirst();
                }
            }
            if (q.isEmpty()) {
                bids.remove(bidPrice);
            }
        }
    }

    private void updateStatus(Order order) {
        if (order.getPendingAmount() == 0.0) {
            order.setStatus(OrderStatus.FILLED);
        } else if (order.getPendingAmount() < order.getAmount()) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        } else {
            order.setStatus(OrderStatus.OPEN);
        }
    }

    public void addOrderToBook(Order order) {
        if (order.getDirection() == OrderDirection.BUY) {
            buyBooks.computeIfAbsent(order.getAsset(), k -> new ConcurrentSkipListMap<>(Comparator.reverseOrder()))
                    .computeIfAbsent(order.getPrice(), p -> new ConcurrentLinkedDeque<>())
                    .addLast(order);
        } else {
            sellBooks.computeIfAbsent(order.getAsset(), k -> new ConcurrentSkipListMap<>())
                    .computeIfAbsent(order.getPrice(), p -> new ConcurrentLinkedDeque<>())
                    .addLast(order);
        }
        log.info("Order added to book: id={} asset={} side={} price={} pending={}",
                order.getId(), order.getAsset(), order.getDirection(), order.getPrice(), order.getPendingAmount());
    }
}
