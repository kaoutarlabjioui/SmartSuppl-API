package org.smartsupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartsupply.dto.request.SalesOrderLineRequestDto;
import org.smartsupply.dto.request.SalesOrderRequestDto;
import org.smartsupply.dto.response.SalesOrderResponseDto;
import org.smartsupply.exception.BusinessException;
import org.smartsupply.exception.ResourceNotFoundException;
import org.smartsupply.exception.StockUnavailableException;
import org.smartsupply.mapper.SalesOrderMapper;
import org.smartsupply.model.entity.*;
import org.smartsupply.model.enums.MovementType;
import org.smartsupply.model.enums.OrderStatus;
import org.smartsupply.repository.*;
import org.smartsupply.service.InventoryService;
import org.smartsupply.service.SalesOrderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SalesOrderServiceImp implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    public SalesOrderResponseDto create(SalesOrderRequestDto request) {

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé: " + request.getClientId()));
        if (!client.getIsActive()) {
            throw new BusinessException("Client inactif: " + request.getClientId());
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse non trouvé: " + request.getWarehouseId()));
        if (!warehouse.getActive()) {
            throw new BusinessException("Warehouse inactif: " + request.getWarehouseId());
        }

        SalesOrder order = SalesOrder.builder()
                .client(client)
                .warehouse(warehouse)
                .status(OrderStatus.CREATED)
                .build();

        List<SalesOrderLine> lines = new ArrayList<>();
        if (request.getLines() != null) {
            for (SalesOrderLineRequestDto lineRequestDto : request.getLines()) {
                Product product = productRepository.findById(lineRequestDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Produit non trouvé: " + lineRequestDto.getProductId()));
                if (!(product.getActive())) {
                    throw new BusinessException("Produit inactif: " + lineRequestDto.getProductId());
                }

                BigDecimal unitPrice = product.getOriginalPrice().add(product.getProfite());
                BigDecimal finalPrice = unitPrice.multiply(BigDecimal.valueOf(lineRequestDto.getQtyOrdered()));
                SalesOrderLine line = SalesOrderLine.builder()
                        .product(product)
                        .qtyOrdered(lineRequestDto.getQtyOrdered())
                        .qtyReserved(0)
                        .price(finalPrice)
                        .salesOrder(order)
                        .build();
                lines.add(line);
            }
        }
        order.setLines(lines);

        SalesOrder saved = salesOrderRepository.save(order);
        log.info("SalesOrder créée id={} clientId={}", saved.getId(), client.getId());
        return salesOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderResponseDto getById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder non trouvée: " + id));
        return salesOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrderResponseDto> listAll(String statusStr, Long clientId, LocalDateTime startDate,
            LocalDateTime endDate, Pageable pageable) {
        OrderStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = OrderStatus.valueOf(statusStr);
            } catch (Exception ex) {
                throw new BusinessException("Status invalide: " + statusStr);
            }
        }

        Page<SalesOrder> page;

        boolean hasStatus = status != null;
        boolean hasClient = clientId != null;
        boolean hasStartEnd = (startDate != null && endDate != null);

        if (hasStatus && hasClient && hasStartEnd) {
            page = salesOrderRepository.findByStatusAndClientIdAndCreatedAtBetween(status, clientId, startDate, endDate,
                    pageable);
        } else if (hasStatus && hasClient) {
            page = salesOrderRepository.findByStatusAndClientId(status, clientId, pageable);
        } else if (hasStatus && hasStartEnd) {
            page = salesOrderRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate, pageable);
        } else if (hasClient && hasStartEnd) {
            page = salesOrderRepository.findByClientIdAndCreatedAtBetween(clientId, startDate, endDate, pageable);
        } else if (hasStatus) {
            page = salesOrderRepository.findByStatus(status, pageable);
        } else if (hasClient) {
            page = salesOrderRepository.findByClientId(clientId, pageable);
        } else if (hasStartEnd) {
            page = salesOrderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        } else {
            page = salesOrderRepository.findAll(pageable);
        }

        return page.map(salesOrderMapper::toResponse);
    }

    @Override
    public SalesOrderResponseDto addLine(Long orderId, SalesOrderLineRequestDto lineRequest) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder non trouvée: " + orderId));

        Product product = productRepository.findById(lineRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product non trouvé: " + lineRequest.getProductId()));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Produit inactif: " + lineRequest.getProductId());
        }
        BigDecimal unitPrice = product.getOriginalPrice().add(product.getProfite());
        BigDecimal finalPrice = unitPrice.multiply(BigDecimal.valueOf(lineRequest.getQtyOrdered()));
        SalesOrderLine line = SalesOrderLine.builder()
                .product(product)
                .qtyOrdered(lineRequest.getQtyOrdered())
                .qtyReserved(0)
                .price(finalPrice)
                .salesOrder(order)
                .build();
        order.getLines().add(line);
        SalesOrder saved = salesOrderRepository.save(order);
        log.info("Ligne ajoutée orderId={} productId={}", orderId, product.getId());
        return salesOrderMapper.toResponse(saved);
    }

    @Override
    public SalesOrderResponseDto updateStatus(Long orderId, String newStatus) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder non trouvée: " + orderId));

        OrderStatus target;
        try {
            target = OrderStatus.valueOf(newStatus);
        } catch (Exception ex) {
            throw new BusinessException("Status invalide: " + newStatus);
        }

        List<String> warnings = new ArrayList<>();
        boolean allLinesReserved = true;

        if (target == OrderStatus.RESERVED && order.getStatus() == OrderStatus.CREATED) {
            log.info("Tentative de réservation pour la commande {} ...", orderId);
            for (SalesOrderLine line : order.getLines()) {
                Long warehouseId = order.getWarehouse().getId();
                Long productId = line.getProduct().getId();
                String productName = line.getProduct().getName();
                int qtyOrdered = line.getQtyOrdered();
                try {
                    inventoryService.smartReserve(productId, warehouseId, qtyOrdered, "SO:" + orderId);
                    line.setQtyReserved(qtyOrdered);
                    log.info(" Produit '{}' réservé avec succès (qty={})", productName, qtyOrdered);
                } catch (StockUnavailableException e) {
                    allLinesReserved = false;
                    String msg = String.format(
                            "Stock insuffisant pour le produit '%s' (id=%d). Commande fournisseur prévue.",
                            productName, productId);
                    warnings.add(msg);
                    log.warn(msg);
                }

            }

        }

        if ((order.getStatus() == OrderStatus.RESERVED)
                && (target == OrderStatus.CANCELED || target == OrderStatus.CREATED)) {
            log.info("Libération des quantités réservées pour la commande {} ...", orderId);

            for (SalesOrderLine line : order.getLines()) {

                int qtyToRelease = line.getQtyReserved();
                if (qtyToRelease <= 0)
                    continue;

                Long warehouseId = order.getWarehouse().getId();
                Long productId = line.getProduct().getId();
                try {
                    Inventory inventory = inventoryRepository
                            .findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Inventory not found for productId=" + productId + " warehouseId=" + warehouseId));
                    inventory.setQtyReserved(inventory.getQtyReserved() - qtyToRelease);
                    if (inventory.getQtyReserved() < 0)
                        inventory.setQtyReserved(0);
                    inventoryRepository.save(inventory);
                    log.info("Libéré {} unités pour le produit '{}' dans warehouse {}", qtyToRelease,
                            line.getProduct().getName(), warehouseId);
                    line.setQtyReserved(0);
                } catch (Exception e) {
                    String msg = String.format("Impossible de libérer le stock pour le produit '%s' (id=%d)",
                            line.getProduct().getName(), productId);
                    warnings.add(msg);
                    log.warn(msg, e);
                }

            }
        }

        if (target == OrderStatus.RESERVED && !allLinesReserved) {
            log.info("Au moins une ligne n'a pas pu être réservée -> passage en statut BACKORDER pour la commande {}",
                    orderId);
            order.setStatus(OrderStatus.BACKORDER);
        } else {
            order.setStatus(target);
        }
        SalesOrder saved = salesOrderRepository.save(order);

        SalesOrderResponseDto dto = salesOrderMapper.toResponse(saved);
        dto.setWarnings(warnings);
        log.info("SalesOrder id={} nouveau status={}", orderId, target);
        return dto;
    }

    @Override
    public void delete(Long id) {
        if (!salesOrderRepository.existsById(id)) {
            throw new ResourceNotFoundException("SalesOrder non trouvée: " + id);
        }
        salesOrderRepository.deleteById(id);
        log.info("SalesOrder supprimée id={}", id);
    }

    @Transactional
    public void planShipment(Long orderId, org.smartsupply.dto.request.ShipmentRequestDto req) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder non trouvée: " + orderId));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new BusinessException("La commande doit être RESERVED pour planifier une expédition.");
        }

        if (shipmentRepository.existsBySalesOrderId(orderId)) {
            throw new BusinessException("Une expédition existe déjà pour cette commande.");
        }

        // Generate tracking number (UUID for now or carrier logic)
        String trackingNumber = "TRK-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Shipment shipment = Shipment.builder()
                .salesOrder(order)
                .warehouse(order.getWarehouse())
                .carrier(req.getCarrier())
                .trackingNumber(trackingNumber)
                .status(org.smartsupply.model.enums.ShipmentStatus.PLANNED)
                .street(req.getStreet())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .country(req.getCountry())
                .build();

        shipmentRepository.save(shipment);
        log.info("Expédition planifiée pour commande {}: Tracking={}", orderId, trackingNumber);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId, String trackingNumber) {
        // Here trackingNumber param is optional if we verify against existing shipment
        // But for compatibility with existing interface, we can ignore it or verify it.

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder non trouvée: " + orderId));

        Shipment shipment = shipmentRepository.findBySalesOrderId(orderId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Aucune expédition planifiée pour la commande " + orderId));

        if (shipment.getStatus() != org.smartsupply.model.enums.ShipmentStatus.PLANNED) {
            throw new BusinessException("L'expédition n'est pas en statut PLANNED.");
        }

        Long warehouseId = order.getWarehouse().getId();

        for (SalesOrderLine line : order.getLines()) {
            Long productId = line.getProduct().getId();
            int qtyToShip = line.getQtyReserved();

            if (qtyToShip <= 0)
                continue;

            Inventory inv = inventoryRepository.findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory introuvable productId=" + productId + " warehouseId=" + warehouseId));

            if (inv.getQtyOnHand() < qtyToShip) {
                // Should rely on reservation but safety check
                throw new BusinessException("Incohérence stock: QtyOnHand < QtyReserved lors de l'expédition.");
            }

            // Déduire du stock et de la réservation
            inv.setQtyOnHand(inv.getQtyOnHand() - qtyToShip);
            inv.setQtyReserved(inv.getQtyReserved() - qtyToShip);
            inventoryRepository.save(inv);

            line.setQtyReserved(0); // Consumption complete

            // Enregistrement du mouvement OUTBOUND
            inventoryMovementRepository.save(InventoryMovement.builder()
                    .inventory(inv)
                    .type(MovementType.OUTBOUND) // Fix typo if exists, assumed OUTBOUND
                    .qty(qtyToShip)
                    .occurredAt(LocalDateTime.now())
                    .reference("SHIP:" + shipment.getTrackingNumber())
                    .build());
        }

        shipment.setStatus(org.smartsupply.model.enums.ShipmentStatus.IN_TRANSIT);
        shipmentRepository.save(shipment);

        order.setStatus(OrderStatus.SHIPPED);
        salesOrderRepository.save(order);

        log.info("Commande {} expédiée via shipment {}", orderId, shipment.getTrackingNumber());
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder non trouvée: " + orderId));

        Shipment shipment = shipmentRepository.findBySalesOrderId(orderId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Aucune expédition trouvée pour la commande " + orderId));

        if (shipment.getStatus() != org.smartsupply.model.enums.ShipmentStatus.IN_TRANSIT) {
            throw new BusinessException("L'expédition doit être IN_TRANSIT pour être livrée.");
        }

        shipment.setStatus(org.smartsupply.model.enums.ShipmentStatus.DELIVERED);
        shipmentRepository.save(shipment);

        order.setStatus(OrderStatus.DELIVERED);
        salesOrderRepository.save(order);

        log.info("Commande {} livrée avec succès.", orderId);
    }
}