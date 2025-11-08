package org.smartsupply.SmartSupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smartsupply.SmartSupply.dto.response.InventoryMovementDto;
import org.smartsupply.SmartSupply.model.entity.InventoryMovement;

@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {

    @Mapping(target = "inventoryId", source = "inventory.id")
    InventoryMovementDto toDto(InventoryMovement m);
}