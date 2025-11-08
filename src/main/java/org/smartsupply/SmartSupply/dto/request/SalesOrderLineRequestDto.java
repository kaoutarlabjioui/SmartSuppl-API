package org.smartsupply.SmartSupply.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLineRequestDto {
    @NotNull
    private Long productId;

    @Min(1)
    private int qtyOrdered;

//    @Min(0)
//    private BigDecimal price;
}