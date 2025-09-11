package com.htm.ome.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htm.ome.dto.OrderRequest;
import com.htm.ome.enums.OrderDirection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TestCasesFromAssignmentDoc {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // The following test cases are mentioned in THA

    @Test
    void testOrderMatchingScenario() throws Exception {
        OrderRequest sellOrder = new OrderRequest();
        sellOrder.setAsset("BTC");
        sellOrder.setPrice(43251.00);
        sellOrder.setAmount(1.0);
        sellOrder.setDirection(OrderDirection.SELL);
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.pendingAmount").value("1.0"))
                .andExpect(jsonPath("$.trades").isEmpty());

        OrderRequest buyLow = new OrderRequest();
        buyLow.setAsset("BTC");
        buyLow.setPrice(43250.00);
        buyLow.setAmount(0.25);
        buyLow.setDirection(OrderDirection.BUY);
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyLow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pendingAmount").value("0.25"))
                .andExpect(jsonPath("$.trades").isEmpty());

        OrderRequest buyHigh = new OrderRequest();
        buyHigh.setAsset("BTC");
        buyHigh.setPrice(43253.00);
        buyHigh.setAmount(0.35);
        buyHigh.setDirection(OrderDirection.BUY);
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyHigh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.pendingAmount").value("0.0"))
                .andExpect(jsonPath("$.trades[0].orderId").value(0))
                .andExpect(jsonPath("$.trades[0].amount").value(0.35));

        mockMvc.perform(get("/orders/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingAmount").value("0.65"))
                .andExpect(jsonPath("$.trades[0].orderId").value(2));

        mockMvc.perform(get("/orders/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingAmount").value("0.0"))
                .andExpect(jsonPath("$.trades[0].orderId").value(0));

        OrderRequest buyRemaining = new OrderRequest();
        buyRemaining.setAsset("BTC");
        buyRemaining.setPrice(43251.00);
        buyRemaining.setAmount(0.65);
        buyRemaining.setDirection(OrderDirection.BUY);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyRemaining)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.pendingAmount").value("0.0"))
                .andExpect(jsonPath("$.trades[0].orderId").value(0))
                .andExpect(jsonPath("$.trades[0].amount").value(0.65));

        mockMvc.perform(get("/orders/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingAmount").value("0.0"))
                .andExpect(jsonPath("$.trades.length()").value(2));
    }
}
