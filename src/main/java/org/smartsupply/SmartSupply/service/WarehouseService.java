package org.smartsupply.SmartSupply.service;

import org.smartsupply.SmartSupply.dto.request.WarehouseRequestDto;
import org.smartsupply.SmartSupply.dto.response.WarehouseDetailDto;
import org.smartsupply.SmartSupply.dto.response.WarehouseSimpleDto;

import java.util.List;

public interface WarehouseService {

    WarehouseSimpleDto createWarehouse(WarehouseRequestDto request);

    List<WarehouseSimpleDto> getAllWarehouses();

    WarehouseDetailDto getWarehouseById(Long id);

    WarehouseSimpleDto updateWarehouse(Long id, WarehouseRequestDto request);

    void deleteWarehouse(Long id);

    boolean existsById(Long id);
}