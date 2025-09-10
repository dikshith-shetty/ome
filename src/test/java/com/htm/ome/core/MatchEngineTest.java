package com.htm.ome.core;

import com.htm.ome.enums.OrderDirection;
import com.htm.ome.enums.OrderStatus;
import com.htm.ome.model.Order;
import com.htm.ome.model.TradeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchEngineTest {

    private MatchEngine matchEngine;

    @BeforeEach
    void setUp() {
        matchEngine = new MatchEngine();
    }

    @Test
    void testAddOrderToBookAndMatchFullFillBuy() {
        Order sell = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(55000.00)
                .amount(1.50)
                .pendingAmount(1.50)
                .direction(OrderDirection.SELL)
                .status(OrderStatus.OPEN)
                .build();
        matchEngine.addOrderToBook(sell);

        Order buy = Order.builder()
                .id(2L)
                .asset("BTC")
                .price(55500.00)
                .amount(1.50)
                .pendingAmount(1.50)
                .direction(OrderDirection.BUY)
                .status(OrderStatus.OPEN)
                .build();

        List<TradeModel> trades = matchEngine.match(buy);

        assertEquals(1, trades.size());
        assertEquals(0.0, buy.getPendingAmount());
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertEquals(0.0, sell.getPendingAmount());
        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(1.50, trades.getFirst().getAmount());
    }

    @Test
    void testPartialFillBuy() {
        Order sell = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(55000.00)
                .amount(1.00)
                .pendingAmount(1.00)
                .direction(OrderDirection.SELL)
                .status(OrderStatus.OPEN)
                .build();
        matchEngine.addOrderToBook(sell);

        Order buy = Order.builder()
                .id(2L)
                .asset("BTC")
                .price(55500.00)
                .amount(2.00)
                .pendingAmount(2.00)
                .direction(OrderDirection.BUY)
                .status(OrderStatus.OPEN)
                .build();

        List<TradeModel> trades = matchEngine.match(buy);

        assertEquals(1, trades.size());
        assertEquals(1.00, buy.getPendingAmount());
        assertEquals(OrderStatus.PARTIALLY_FILLED, buy.getStatus());
        assertEquals(0.0, sell.getPendingAmount());
        assertEquals(OrderStatus.FILLED, sell.getStatus());
    }

    @Test
    void testSellMatchingAgainstMultipleBids() {
        Order buy1 = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(55500.00)
                .amount(1.00)
                .pendingAmount(1.00)
                .direction(OrderDirection.BUY)
                .status(OrderStatus.OPEN)
                .build();
        Order buy2 = Order.builder()
                .id(2L)
                .asset("BTC")
                .price(55600.00)
                .amount(1.00)
                .pendingAmount(1.00)
                .direction(OrderDirection.BUY)
                .status(OrderStatus.OPEN)
                .build();
        matchEngine.addOrderToBook(buy1);
        matchEngine.addOrderToBook(buy2);

        Order sell = Order.builder()
                .id(3L)
                .asset("BTC")
                .price(55000.00)
                .amount(2.00)
                .pendingAmount(2.00)
                .direction(OrderDirection.SELL)
                .status(OrderStatus.OPEN)
                .build();

        List<TradeModel> trades = matchEngine.match(sell);

        assertEquals(2, trades.size());
        assertEquals(0.0, sell.getPendingAmount());
        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(OrderStatus.FILLED, buy1.getStatus());
        assertEquals(OrderStatus.FILLED, buy2.getStatus());
        assertEquals(1.0, trades.get(0).getAmount());
        assertEquals(1.0, trades.get(1).getAmount());
    }

    @Test
    void testNoMatchingPrice() {
        Order sell = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(56000.00)
                .amount(1.0)
                .pendingAmount(1.0)
                .direction(OrderDirection.SELL)
                .status(OrderStatus.OPEN)
                .build();
        matchEngine.addOrderToBook(sell);

        Order buy = Order.builder()
                .id(2L)
                .asset("BTC")
                .price(55000.00)
                .amount(1.0)
                .pendingAmount(1.0)
                .direction(OrderDirection.BUY)
                .status(OrderStatus.OPEN)
                .build();

        List<TradeModel> trades = matchEngine.match(buy);

        assertTrue(trades.isEmpty());
        assertEquals(1.0, buy.getPendingAmount());
        assertEquals(OrderStatus.OPEN, buy.getStatus());
    }
}
