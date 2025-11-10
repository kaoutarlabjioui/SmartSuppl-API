package org.smartsupply.SmartSupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartsupply.SmartSupply.exception.BusinessException;
import org.smartsupply.SmartSupply.exception.ResourceNotFoundException;
import org.smartsupply.SmartSupply.exception.StockUnavailableException;
import org.smartsupply.SmartSupply.model.entity.Inventory;
import org.smartsupply.SmartSupply.model.entity.InventoryMovement;
import org.smartsupply.SmartSupply.model.entity.Product;
import org.smartsupply.SmartSupply.model.entity.Warehouse;
import org.smartsupply.SmartSupply.model.enums.MovementType;
import org.smartsupply.SmartSupply.repository.InventoryMovementRepository;
import org.smartsupply.SmartSupply.repository.InventoryRepository;
import org.smartsupply.SmartSupply.repository.ProductRepository;
import org.smartsupply.SmartSupply.repository.WarehouseRepository;
import org.smartsupply.SmartSupply.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImp implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMovementRepository movementRepository;
    // private final ReservationRepository reservationRepository; // décommenter si vous ajoutez Reservation entity

    @Override
    @Transactional
    public void ensureInventoryExists(Long productId, Long warehouseId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found " + productId);
        }
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse not found " + warehouseId);
        }
        if (!inventoryRepository.existsByProductIdAndWarehouseId(productId, warehouseId)) {
            Product p = productRepository.getReferenceById(productId);
            Warehouse w = warehouseRepository.getReferenceById(warehouseId);
            Inventory inv = Inventory.builder()
                    .product(p)
                    .warehouse(w)
                    .qtyOnHand(0)
                    .qtyReserved(0)
                    .build();
            inventoryRepository.save(inv);
        }
    }

    @Override
    @Transactional
    public void inbound(Long productId, Long warehouseId, Integer qty, String reference) {
        Inventory inv = inventoryRepository.findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId=" + productId + " warehouseId=" + warehouseId));
        inv.setQtyOnHand(inv.getQtyOnHand() + qty);
        inventoryRepository.save(inv);

        movementRepository.save(InventoryMovement.builder()
                .inventory(inv)
                .type(MovementType.INBOUND)
                .qty(qty)
                .occurredAt(LocalDateTime.now())
                .reference(reference)
                .build());
    }

    @Override
    @Transactional
    public void outbound(Long productId, Long warehouseId, Integer qty, String reference) {
        Inventory inv = inventoryRepository.findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId=" + productId + " warehouseId=" + warehouseId));

        int available = inv.getQtyOnHand() - inv.getQtyReserved();
        if (available < qty) {
            throw new StockUnavailableException("Stock insuffisant. Disponible: " + available + ", demandé: " + qty);
        }

        inv.setQtyOnHand(inv.getQtyOnHand() - qty);
        inventoryRepository.save(inv);

        movementRepository.save(InventoryMovement.builder()
                .inventory(inv)
                .type(MovementType.OUTBOUND)
                .qty(qty)
                .occurredAt(LocalDateTime.now())
                .reference(reference)
                .build());
    }

    @Override
    @Transactional
    public void adjustment(Long productId, Long warehouseId, Integer qty, String reference) {
        Inventory inv = inventoryRepository.findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId=" + productId + " warehouseId=" + warehouseId));

        int newQtyOnHand = inv.getQtyOnHand() + qty; // qty can be negative
        if (newQtyOnHand < inv.getQtyReserved()) {
            throw new BusinessException("Ajustement invalide: qtyOnHand < qtyReserved");
        }
        inv.setQtyOnHand(newQtyOnHand);
        inventoryRepository.save(inv);

        movementRepository.save(InventoryMovement.builder()
                .inventory(inv)
                .type(MovementType.ADJUSTMENT)
                .qty(qty)
                .occurredAt(LocalDateTime.now())
                .reference(reference)
                .build());
    }
    public void smartReserve(Long productId , Long mainWarehouseId,Integer qty,String reference){
        int availableMain = getAvailable(productId,mainWarehouseId);

        if(availableMain >= qty){
            reserve(productId,mainWarehouseId,qty,reference,3600);
            log.info("Réservé entièrement dans le warehouse principal {}" , mainWarehouseId);
            return;
        }

        if(availableMain > 0){
            reserve(productId,mainWarehouseId,availableMain,reference,3600);
            qty-=availableMain;
            log.info("Réservé partiellement {} dans warehouse principal {}", availableMain, mainWarehouseId);
        }

        List<Long> otherWarehouses = warehouseRepository.findAllIdsExcept(mainWarehouseId);

        for(Long wId : otherWarehouses){
            int available = getAvailable(productId,wId);
            if (available<=0) continue;

            int toReserve = Math.min(qty,available);

            log.info("Transfert de {} unités de warehouse {} vers {}", toReserve, wId, mainWarehouseId);
            transfer(productId, wId, mainWarehouseId, toReserve, reference);

            reserve(productId,mainWarehouseId,toReserve,reference,3600);
            qty -= toReserve;
            log.info("Réservé {} après transfert depuis warehouse {}", toReserve, wId);

            if (qty <= 0) break;
        }

        if (qty > 0) {

            throw new StockUnavailableException("Stock insuffisant pour productId=" + productId + " (reste à réserver: " + qty + ")");
        }


    }
    @Override
    @Transactional
    public String reserve(Long productId, Long warehouseId, Integer qty, String sourceRef, long ttlSeconds) {
        Inventory inv = inventoryRepository.findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId=" + productId + " warehouseId=" + warehouseId));

        int available = inv.getQtyOnHand() - inv.getQtyReserved();
        if (available < qty) {
            throw new StockUnavailableException("Stock insuffisant pour réservation. Disponible: " + available);
        }

        inv.setQtyReserved(inv.getQtyReserved() + qty);
        inventoryRepository.save(inv);


        movementRepository.save(InventoryMovement.builder()
                .inventory(inv)
                .type(MovementType.ADJUSTMENT)
                .qty(qty)
                .occurredAt(LocalDateTime.now())
                .reference(sourceRef)
                .build());


        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    public void releaseReservation(Long reservationId) {

        throw new UnsupportedOperationException("releaseReservation is not implemented. Implement Reservation entity for TTL.");
    }

    @Override
    @Transactional
    public void transfer(Long productId, Long sourceWarehouseId, Long targetWarehouseId, Integer qty, String reference) {
        if (sourceWarehouseId.equals(targetWarehouseId)) {
            throw new BusinessException("Source and target warehouses must differ");
        }

        Inventory sourceInv = inventoryRepository.findWithLockByProductIdAndWarehouseId(productId, sourceWarehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Source inventory not found for productId=" + productId + " warehouseId=" + sourceWarehouseId));

        int available = sourceInv.getQtyOnHand() - sourceInv.getQtyReserved();
        if (available < qty) {
            throw new BusinessException("Stock insuffisant dans l'entrepôt source. Disponible: " + available);
        }

        sourceInv.setQtyOnHand(sourceInv.getQtyOnHand() - qty);
        inventoryRepository.save(sourceInv);
        movementRepository.save(InventoryMovement.builder()
                .inventory(sourceInv)
                .type(MovementType.OUTBOUND)
                .qty(qty)
                .occurredAt(LocalDateTime.now())
                .reference(reference)
                .build());

        Inventory targetInv = inventoryRepository.findWithLockByProductIdAndWarehouseId(productId, targetWarehouseId)
                .orElseGet(() -> {
                    Inventory inv = Inventory.builder()
                            .product(sourceInv.getProduct())
                            .warehouse(warehouseRepository.getReferenceById(targetWarehouseId))
                            .qtyOnHand(0)
                            .qtyReserved(0)
                            .build();
                    return inventoryRepository.save(inv);
                });

        targetInv.setQtyOnHand(targetInv.getQtyOnHand() + qty);
        inventoryRepository.save(targetInv);
        movementRepository.save(InventoryMovement.builder()
                .inventory(targetInv)
                .type(MovementType.INBOUND)
                .qty(qty)
                .occurredAt(LocalDateTime.now())
                .reference(reference)
                .build());
    }

    @Override
    public List<Long> findWarehousesWithAvailable(Long productId) {
        return inventoryRepository.findWarehouseIdsWithAvailable(productId);
    }

    @Override
    public Integer getAvailable(Long productId, Long warehouseId) {
        Integer v = inventoryRepository.findAvailableByProductIdAndWarehouseId(productId, warehouseId);
        return v == null ? 0 : v;
    }
}