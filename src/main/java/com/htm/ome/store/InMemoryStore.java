package com.htm.ome.store;

import com.htm.ome.model.Order;
import com.htm.ome.model.TradeModel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStore {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final Map<Long, TradeModel> trades = new ConcurrentHashMap<>();

    private final AtomicLong orderIdGen = new AtomicLong(-1);
    private final AtomicLong tradeIdGen = new AtomicLong(50000);

    public long nextOrderId() {
        return orderIdGen.incrementAndGet();
    }

    public long nextTradeId() {
        return tradeIdGen.incrementAndGet();
    }

    public void saveOrder(Order order) {
        orders.put(order.getId(), order);
    }

    public Order getOrder(Long id) {
        return orders.get(id);
    }

    public Collection<Order> allOrders() {
        return orders.values();
    }

    public void saveTrade(TradeModel tradeModel) {
        trades.put(tradeModel.getId(), tradeModel);
    }

    public Collection<TradeModel> allTrades() {
        return trades.values();
    }

    public long tradeCount() {
        return trades.size();
    }
}
