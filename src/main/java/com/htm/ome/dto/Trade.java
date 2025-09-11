package com.htm.ome.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
public class Trade {
    private Long orderId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double price;
}
