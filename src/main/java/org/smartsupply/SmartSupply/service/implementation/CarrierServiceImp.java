package org.smartsupply.SmartSupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartsupply.SmartSupply.dto.request.CarrierRequestDto;
import org.smartsupply.SmartSupply.dto.response.CarrierResponseDto;
import org.smartsupply.SmartSupply.mapper.CarrierMapper;
import org.smartsupply.SmartSupply.model.entity.Carrier;
import org.smartsupply.SmartSupply.repository.CarrierRepository;
import org.smartsupply.SmartSupply.service.CarrierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CarrierServiceImp implements CarrierService {

    private final CarrierRepository carrierRepository;
    private final CarrierMapper carrierMapper;


    public CarrierResponseDto createCarrier(CarrierRequestDto carrierRequestDto){
        log.info("Creating a new carrier: {}", carrierRequestDto.getName());


       Carrier carrier = carrierMapper.toEntity(carrierRequestDto);
       Carrier savedCarrier = carrierRepository.save(carrier);

        log.info("Carrier created successfully. ID: {}", savedCarrier.getId());

        return carrierMapper.toResponseDto(savedCarrier);



    }



}
