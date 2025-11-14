package org.smartsupply.SmartSupply.service;

import org.smartsupply.SmartSupply.dto.request.CarrierRequestDto;
import org.smartsupply.SmartSupply.dto.response.CarrierResponseDto;
import org.smartsupply.SmartSupply.dto.response.CarrierSimpleDto;

import java.util.List;

public interface CarrierService {

    CarrierResponseDto createCarrier(CarrierRequestDto carrierRequestDto );
    List<CarrierResponseDto> getAllCarriers();
    CarrierResponseDto getCarrierById(Long id);
    CarrierResponseDto updateCarrier(Long id, CarrierRequestDto dto);
    void deleteCarrier(Long id);
    List<CarrierResponseDto> searchCarriers(String q);
    boolean existsById(Long id);
    List<CarrierSimpleDto> getAllSimple();
}
