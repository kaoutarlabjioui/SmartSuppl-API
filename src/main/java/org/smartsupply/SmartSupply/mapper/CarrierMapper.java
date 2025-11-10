package org.smartsupply.SmartSupply.mapper;


import org.mapstruct.Mapper;
import org.smartsupply.SmartSupply.dto.request.CarrierRequestDto;
import org.smartsupply.SmartSupply.dto.response.CarrierResponseDto;
import org.smartsupply.SmartSupply.model.entity.Carrier;

@Mapper(componentModel = "spring")
public interface CarrierMapper {

    Carrier toEntity(CarrierRequestDto carrierRequestDto);

    CarrierResponseDto toResponseDto(Carrier carrier);





}
