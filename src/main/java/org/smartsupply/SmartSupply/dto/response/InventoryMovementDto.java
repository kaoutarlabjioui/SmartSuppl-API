package org.smartsupply.SmartSupply.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import org.smartsupply.SmartSupply.model.enums.MovementType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementDto {
    private Long id;
    private MovementType type;
    private Integer qty;
    private LocalDateTime occurredAt;
    private String reference;
    private Long inventoryId;
}