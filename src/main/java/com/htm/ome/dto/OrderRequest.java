package com.htm.ome.dto;

import com.htm.ome.enums.OrderDirection;
import com.htm.ome.validation.ValidAsset;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderRequest {
    @NotBlank(message = "asset is required")
    @ValidAsset
    private String asset;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "price must be >= 0.01")
    @Digits(integer = 12, fraction = 2, message = "price must have up to 2 decimal places")
    private Double price;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "amount must be >= 0.01")
    @Digits(integer = 20, fraction = 2, message = "amount must have up to 2 decimal places")
    private Double amount;

    @NotNull(message = "direction is required")
    private OrderDirection direction;
}
