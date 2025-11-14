package org.smartsupply.SmartSupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.smartsupply.SmartSupply.dto.request.SupplierRequestDto;
import org.smartsupply.SmartSupply.dto.request.SupplierUpdateDto;
import org.smartsupply.SmartSupply.dto.response.SupplierResponseDto;
import org.smartsupply.SmartSupply.dto.response.SupplierSimpleDto;
import org.smartsupply.SmartSupply.model.entity.Supplier;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    Supplier toEntity(SupplierRequestDto dto);
    SupplierResponseDto toResponseDto(Supplier supplier);
    SupplierSimpleDto toSimpleDto(Supplier supplier);
    List<SupplierResponseDto> toResponseDtoList(List<Supplier> suppliers);
    void updateEntityFromDto(SupplierUpdateDto dto, @MappingTarget Supplier supplier);
}