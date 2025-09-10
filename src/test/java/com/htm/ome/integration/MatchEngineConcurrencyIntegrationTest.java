package com.htm.ome.integration;

import com.htm.ome.dto.OrderRequest;
import com.htm.ome.enums.OrderDirection;
import com.htm.ome.service.OrderService;
import com.htm.ome.store.InMemoryStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {"ome.assets=BTC,ETH"})
public class MatchEngineConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InMemoryStore store;

    @Test
    void concurrentMatchingShouldProduceTrades() throws Exception {
        int pairs = 50;
        ExecutorService exec = Executors.newFixedThreadPool(5);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(pairs * 2);
        List<Callable<Void>> tasks = getCallables(pairs, start, done);
        for (Callable<Void> c : tasks) exec.submit(c);
        start.countDown();
        boolean finished = done.await(2, TimeUnit.SECONDS);
        assertTrue(finished, "Submission timeout");
        Thread.sleep(2000);
        assertTrue(store.tradeCount() >= pairs);
    }

    private List<Callable<Void>> getCallables(int pairs, CountDownLatch start, CountDownLatch done) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            tasks.add(() -> {
                start.await();
                OrderRequest r = new OrderRequest();
                r.setAsset("BTC");
                r.setPrice(100.00);
                r.setAmount(1.00);
                r.setDirection(OrderDirection.BUY);
                orderService.createOrder(r);
                done.countDown();
                return null;
            });
            tasks.add(() -> {
                start.await();
                OrderRequest r = new OrderRequest();
                r.setAsset("BTC");
                r.setPrice(100.00);
                r.setAmount(1.00);
                r.setDirection(OrderDirection.SELL);
                orderService.createOrder(r);
                done.countDown();
                return null;
            });
        }
        return tasks;
    }
}
