package com.htm.ome.model;

import com.htm.ome.enums.OrderDirection;
import com.htm.ome.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class Order {
    private Long id;
    private String asset;
    private Double price;
    private Double amount;
    private Double pendingAmount;
    private OrderDirection direction;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;
}
