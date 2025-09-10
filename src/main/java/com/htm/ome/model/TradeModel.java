package com.htm.ome.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TradeModel {
    private Long id;
    private Long buyOrderId;
    private Long sellOrderId;
    private Double amount;
    private Double price;
    private LocalDateTime createdAt;
}
