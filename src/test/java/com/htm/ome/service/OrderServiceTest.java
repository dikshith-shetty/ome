package com.htm.ome.service;

import com.htm.ome.core.MatchEngine;
import com.htm.ome.dto.OrderRequest;
import com.htm.ome.dto.OrderResponse;
import com.htm.ome.enums.OrderDirection;
import com.htm.ome.enums.OrderStatus;
import com.htm.ome.model.Order;
import com.htm.ome.model.TradeModel;
import com.htm.ome.store.InMemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private InMemoryStore store;
    private MatchEngine matchEngine;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        store = mock(InMemoryStore.class);
        matchEngine = mock(MatchEngine.class);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        orderService = new OrderService(store, matchEngine, executorService);

        when(store.nextOrderId()).thenReturn(1L);
        when(store.nextTradeId()).thenReturn(100L);
    }

    @Test
    void testCreateOrderWithoutTrades() {
        OrderRequest req = new OrderRequest();
        req.setAsset("BTC");
        req.setPrice(45345.0);
        req.setAmount(3.0);
        req.setDirection(OrderDirection.BUY);

        when(matchEngine.match(any(Order.class))).thenReturn(List.of());

        OrderResponse response = orderService.createOrder(req);

        assertEquals(1L, response.getId());
        assertEquals("BTC", response.getAsset());
        assertEquals(45345.0, response.getPrice());
        assertEquals(3.0, response.getAmount());
        assertEquals(3.0, response.getPendingAmount());
        assertEquals(OrderDirection.BUY, response.getDirection());
        assertTrue(response.getTrades().isEmpty());

        verify(store).saveOrder(any(Order.class));
    }

    @Test
    void testCreateOrderWithTrades() {
        OrderRequest req = new OrderRequest();
        req.setAsset("BTC");
        req.setPrice(45345.0);
        req.setAmount(1.0);
        req.setDirection(OrderDirection.SELL);

        TradeModel trade = TradeModel.builder()
                .buyOrderId(2L)
                .sellOrderId(1L)
                .price(45345.0)
                .amount(0.5)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        when(matchEngine.match(any(Order.class))).thenReturn(List.of(trade));

        OrderResponse response = orderService.createOrder(req);

        assertEquals(1L, response.getId());
        assertEquals("BTC", response.getAsset());
        assertEquals(OrderDirection.SELL, response.getDirection());
        assertEquals(1.0, response.getAmount());
        assertEquals(1, response.getTrades().size());

        assertEquals(0.5, response.getTrades().getFirst().getAmount());
        assertEquals(45345.0, response.getTrades().getFirst().getPrice());

        verify(store).saveOrder(any(Order.class));
        verify(store).saveTrade(any(TradeModel.class));
    }

    @Test
    void testGetOrderSuccess() {
        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(45345.0)
                .amount(1.0)
                .pendingAmount(0.5)
                .direction(OrderDirection.BUY)
                .status(OrderStatus.PARTIALLY_FILLED)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .modifiedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        when(store.getOrder(1L)).thenReturn(order);
        when(store.allTrades()).thenReturn(List.of());

        OrderResponse response = orderService.getOrder(1L);

        assertEquals(1L, response.getId());
        assertEquals("BTC", response.getAsset());
        assertEquals(45345.0, response.getPrice());
        assertEquals(0.5, response.getPendingAmount());
    }

    @Test
    void testGetOrderNotFound() {
        when(store.getOrder(199L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> orderService.getOrder(199L));
        assertTrue(ex.getMessage().contains("Order not found"));
    }
}
