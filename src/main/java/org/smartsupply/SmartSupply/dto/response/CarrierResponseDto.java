package org.smartsupply.SmartSupply.dto.response;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarrierResponseDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private BigDecimal shippingRate;

}
