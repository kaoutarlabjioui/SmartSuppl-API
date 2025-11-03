package org.smartsupply.SmartSupply.dto.response;

import lombok.*;
import org.smartsupply.SmartSupply.dto.response.ProductSummaryDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDetailResponseDto {
    private Long id;
    private String name;
    private Integer productCount;
    private List<ProductSummaryDto> products;
}