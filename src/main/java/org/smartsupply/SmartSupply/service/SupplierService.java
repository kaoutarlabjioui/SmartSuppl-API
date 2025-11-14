package org.smartsupply.SmartSupply.service;

import org.smartsupply.SmartSupply.dto.request.SupplierRequestDto;
import org.smartsupply.SmartSupply.dto.request.SupplierUpdateDto;
import org.smartsupply.SmartSupply.dto.response.SupplierResponseDto;
import org.smartsupply.SmartSupply.dto.response.SupplierSimpleDto;

import java.util.List;

public interface SupplierService {
    SupplierResponseDto createSupplier(SupplierRequestDto dto);
    List<SupplierResponseDto> getAllSuppliers();
    SupplierResponseDto getSupplierById(Long id);
    SupplierResponseDto updateSupplier(Long id, SupplierUpdateDto dto);
    void deleteSupplier(Long id);
    List<SupplierSimpleDto> getAllSimple();
    List<SupplierResponseDto> search(String q);
    boolean existsById(Long id);
}