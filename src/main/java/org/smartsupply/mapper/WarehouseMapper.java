package org.smartsupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smartsupply.dto.response.WarehouseDetailDto;
import org.smartsupply.dto.response.WarehouseSimpleDto;
import org.smartsupply.model.entity.Warehouse;

@Mapper(componentModel = "spring", uses = {InventoryMapper.class})
public interface WarehouseMapper {

    WarehouseSimpleDto toSimpleDto(Warehouse warehouse);

    WarehouseDetailDto toDetailDto(Warehouse warehouse);

    // exposes inventoryMapper for SpEL in mapping expression
//    InventoryMapper inventoryMapper = null;
}