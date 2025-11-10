package org.smartsupply.SmartSupply.service;

import org.smartsupply.SmartSupply.dto.request.CarrierRequestDto;
import org.smartsupply.SmartSupply.dto.response.CarrierResponseDto;

public interface CarrierService {

    CarrierResponseDto createCarrier(CarrierRequestDto carrierRequestDto );

}
