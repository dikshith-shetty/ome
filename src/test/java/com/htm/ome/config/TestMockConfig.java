package com.htm.ome.config;

import com.htm.ome.service.OrderService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMockConfig {

    @Bean("orderServiceMockBean")
    public OrderService orderService() {
        return Mockito.mock(OrderService.class);
    }
}
