package com.htm.ome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htm.ome.config.TestMockConfig;
import com.htm.ome.dto.OrderRequest;
import com.htm.ome.dto.OrderResponse;
import com.htm.ome.enums.OrderDirection;
import com.htm.ome.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(TestMockConfig.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("orderServiceMockBean")
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public List<String> allowedAssets() {
            return List.of("BTC", "TST");
        }
    }

    private OrderResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = OrderResponse.builder()
                .id(1L)
                .asset("BTC")
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .price(55000.00)
                .amount(1.50)
                .pendingAmount(1.50)
                .direction(OrderDirection.BUY)
                .trades(Collections.emptyList())
                .build();
    }

    @Test
    void createOrderShouldReturnOrderResponse() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setAsset("BTC");
        request.setPrice(55000.00);
        request.setAmount(1.50);
        request.setDirection(OrderDirection.BUY);

        Mockito.when(orderService.createOrder(any(OrderRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.asset").value("BTC"))
                .andExpect(jsonPath("$.price").value(55000.00))
                .andExpect(jsonPath("$.amount").value(1.50))
                .andExpect(jsonPath("$.direction").value("BUY"))
                .andExpect(jsonPath("$.pendingAmount").value(1.50));
    }

    @Test
    void getOrderShouldReturnOrderResponse() throws Exception {
        Mockito.when(orderService.getOrder(eq(1L))).thenReturn(sampleResponse);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.asset").value("BTC"))
                .andExpect(jsonPath("$.price").value(55000.00))
                .andExpect(jsonPath("$.amount").value(1.50))
                .andExpect(jsonPath("$.direction").value("BUY"))
                .andExpect(jsonPath("$.pendingAmount").value(1.50));
    }

    @Test
    void createOrderShouldReturnBadRequestWhenInvalidAsset() throws Exception {
        OrderRequest invalidRequest = new OrderRequest();
        invalidRequest.setAsset("ABC");
        invalidRequest.setPrice(1.00);
        invalidRequest.setAmount(1.50);
        invalidRequest.setDirection(OrderDirection.BUY);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderShouldReturnBadRequestWhenInvalidPrice() throws Exception {
        OrderRequest invalidRequest = new OrderRequest();
        invalidRequest.setAsset("BTC");
        invalidRequest.setPrice(0.0);
        invalidRequest.setAmount(1.50);
        invalidRequest.setDirection(OrderDirection.BUY);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderShouldReturnBadRequestWhenInvalidAmount() throws Exception {
        OrderRequest invalidRequest = new OrderRequest();
        invalidRequest.setAsset("BTC");
        invalidRequest.setPrice(1.0);
        invalidRequest.setAmount(-1.50);
        invalidRequest.setDirection(OrderDirection.BUY);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderShouldReturnBadRequestWhenInvalidDirection() throws Exception {
        String orderRequestJson = "{\n" +
                "  \"asset\": \"string\",\n" +
                "  \"price\": 0.1,\n" +
                "  \"amount\": 0.1,\n" +
                "  \"direction\": \"RANDOM\"\n" +
                "}";

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(status().isBadRequest());
    }
}
