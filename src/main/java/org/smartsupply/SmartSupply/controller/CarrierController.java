package org.smartsupply.SmartSupply.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartsupply.SmartSupply.dto.request.CarrierRequestDto;
import org.smartsupply.SmartSupply.dto.response.CarrierResponseDto;
import org.smartsupply.SmartSupply.service.CarrierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carriers")
@RequiredArgsConstructor
@CrossOrigin
public class CarrierController {

    private final CarrierService carrierService;

    @PostMapping
    public ResponseEntity<CarrierResponseDto>createCarrier(@Valid @RequestBody CarrierRequestDto carrierRequestDto){

        CarrierResponseDto responseDto = carrierService.createCarrier(carrierRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }



}
