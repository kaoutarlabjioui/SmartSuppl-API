package org.smartsupply.SmartSupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smartsupply.SmartSupply.dto.response.WarehouseDetailDto;
import org.smartsupply.SmartSupply.dto.response.WarehouseSimpleDto;
import org.smartsupply.SmartSupply.model.entity.Warehouse;

@Mapper(componentModel = "spring", uses = {InventoryMapper.class})
public interface WarehouseMapper {

    WarehouseSimpleDto toSimpleDto(Warehouse warehouse);

    @Mapping(target = "inventories", expression = "java(inventoryMapper.toSummaryDtoList(warehouse.getInventories()))")
    WarehouseDetailDto toDetailDto(Warehouse warehouse);

    // exposes inventoryMapper for SpEL in mapping expression
    InventoryMapper inventoryMapper = null;
}