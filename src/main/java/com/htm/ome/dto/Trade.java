package com.htm.ome.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Trade {
    private Long orderId;
    private Double amount;
    private Double price;
}
