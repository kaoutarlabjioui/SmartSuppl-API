package org.smartsupply.SmartSupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smartsupply.SmartSupply.dto.response.InventorySummaryDto;
import org.smartsupply.SmartSupply.model.entity.Inventory;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "inventoryId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "qtyOnHand", source = "qtyOnHand")
    @Mapping(target = "qtyReserved", source = "qtyReserved")
    @Mapping(target = "available", expression = "java(inv.getQtyOnHand() - inv.getQtyReserved())")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    InventorySummaryDto toSummaryDto(Inventory inv);

    default List<InventorySummaryDto> toSummaryDtoList(List<Inventory> inventories) {
        if (inventories == null) return null;
        return inventories.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }
    // helper used by services if needed to map a single inventory
//    default Integer calcAvailable(Inventory inv) {
//        if (inv == null) return 0;
//        return inv.getQtyOnHand() - inv.getQtyReserved();
//    }
}