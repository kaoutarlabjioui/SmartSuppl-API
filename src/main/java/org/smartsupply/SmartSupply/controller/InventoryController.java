package org.smartsupply.SmartSupply.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartsupply.SmartSupply.annotation.RequireAuth;
import org.smartsupply.SmartSupply.annotation.RequireRole;
import org.smartsupply.SmartSupply.dto.request.InventoryRequestDto;
import org.smartsupply.SmartSupply.dto.response.InventorySummaryDto;
import org.smartsupply.SmartSupply.mapper.InventoryMapper;
import org.smartsupply.SmartSupply.model.entity.Inventory;
import org.smartsupply.SmartSupply.model.enums.Role;
import org.smartsupply.SmartSupply.repository.InventoryRepository;
import org.smartsupply.SmartSupply.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor

public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;


    @PostMapping("/ensure")
    @RequireRole({Role.ADMIN, Role.WAREHOUSE_MANAGER})
    public ResponseEntity<Void> ensure(@Valid @RequestBody InventoryRequestDto req) {
        inventoryService.ensureInventoryExists(req.getProductId(), req.getWarehouseId());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/inbound")
    @RequireRole({Role.ADMIN, Role.WAREHOUSE_MANAGER})
    public ResponseEntity<Void> inbound(@Valid @RequestBody InventoryRequestDto req) {
        inventoryService.inbound(req.getProductId(), req.getWarehouseId(), req.getQty(), req.getReference());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/outbound")
    @RequireRole({Role.ADMIN, Role.WAREHOUSE_MANAGER})
    public ResponseEntity<Void> outbound(@Valid @RequestBody InventoryRequestDto req) {
        inventoryService.outbound(req.getProductId(), req.getWarehouseId(), req.getQty(), req.getReference());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/adjustment")
    @RequireRole({Role.ADMIN, Role.WAREHOUSE_MANAGER})
    public ResponseEntity<Void> adjustment(@Valid @RequestBody InventoryRequestDto req) {
        inventoryService.adjustment(req.getProductId(), req.getWarehouseId(), req.getQty(), req.getReference());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/reserve")
    @RequireRole({Role.ADMIN, Role.WAREHOUSE_MANAGER})
    public ResponseEntity<?> reserve(@Valid @RequestBody InventoryRequestDto req,
                                     @RequestParam(name = "ttlSeconds", required = false, defaultValue = "86400") long ttlSeconds) {
        String reservationId = inventoryService.reserve(req.getProductId(), req.getWarehouseId(), req.getQty(), req.getReference(), ttlSeconds);
        return ResponseEntity.ok().body(java.util.Map.of("reservationId", reservationId));
    }



    @PostMapping("/transfer")
    @RequireRole({Role.ADMIN, Role.WAREHOUSE_MANAGER})
    public ResponseEntity<java.util.Map<String, String>> transfer(
            @RequestParam Long productId,
            @RequestParam Long sourceWarehouseId,
            @RequestParam Long targetWarehouseId,
            @RequestParam Integer qty,
            @RequestParam String reference) {
        inventoryService.transfer(productId, sourceWarehouseId, targetWarehouseId, qty, reference);
        return ResponseEntity.ok(java.util.Map.of("message", "Transfer executed"));
    }


    @GetMapping("/product/{productId}")
    @RequireAuth
    public ResponseEntity<List<InventorySummaryDto>> getByProduct(@PathVariable Long productId) {
        List<Inventory> invs = inventoryRepository.findByProductId(productId);
        return ResponseEntity.ok(inventoryMapper.toSummaryDtoList(invs));
    }


    @GetMapping("/warehouse/{warehouseId}")
    @RequireAuth
    public ResponseEntity<List<InventorySummaryDto>> getByWarehouse(@PathVariable Long warehouseId) {
        List<Inventory> invs = inventoryRepository.findByWarehouseId(warehouseId);
        return ResponseEntity.ok(inventoryMapper.toSummaryDtoList(invs));
    }
}