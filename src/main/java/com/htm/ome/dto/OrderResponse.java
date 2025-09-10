package com.htm.ome.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.htm.ome.enums.OrderDirection;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class OrderResponse {
    private Long id;

    private OffsetDateTime timestamp;

    private String asset;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double pendingAmount;

    private OrderDirection direction;

    private List<Trade> trades;
}
