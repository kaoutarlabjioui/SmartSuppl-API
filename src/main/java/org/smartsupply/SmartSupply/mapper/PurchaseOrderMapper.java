package org.smartsupply.SmartSupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smartsupply.SmartSupply.dto.response.*;
import org.smartsupply.SmartSupply.model.entity.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    PurchaseOrderResponseDto toResponseDto(PurchaseOrder po);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    POLineResponseDto toLineResponse(POLine line);

    List<POLineResponseDto> toLineResponseList(List<POLine> lines);
}