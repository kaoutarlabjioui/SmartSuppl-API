package org.smartsupply.SmartSupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartsupply.SmartSupply.dto.request.SalesOrderLineRequestDto;
import org.smartsupply.SmartSupply.dto.request.SalesOrderRequestDto;
import org.smartsupply.SmartSupply.dto.response.SalesOrderResponseDto;
import org.smartsupply.SmartSupply.exception.BusinessException;
import org.smartsupply.SmartSupply.exception.ResourceNotFoundException;
import org.smartsupply.SmartSupply.mapper.SalesOrderMapper;
import org.smartsupply.SmartSupply.model.entity.*;
import org.smartsupply.SmartSupply.model.enums.OrderStatus;
import org.smartsupply.SmartSupply.repository.*;
import org.smartsupply.SmartSupply.service.SalesOrderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public SalesOrderResponseDto create(SalesOrderRequestDto request) {

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé: " + request.getClientId()));
        if (!Boolean.TRUE.equals(client.getIsActive())) {
            throw new BusinessException("Client inactif: " + request.getClientId());
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse non trouvé: " + request.getWarehouseId()));
        if (!Boolean.TRUE.equals(warehouse.getActive())) {
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
                        .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + lineRequestDto.getProductId()));
                if (!Boolean.TRUE.equals(product.getActive())) {
                    throw new BusinessException("Produit inactif: " + lineRequestDto.getProductId());
                }

                BigDecimal finalPrice = product.getOriginalPrice().add(product.getProfite());
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
    public Page<SalesOrderResponseDto> listAll(String statusStr, Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
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
            page = salesOrderRepository.findByStatusAndClientIdAndCreatedAtBetween(status, clientId, startDate, endDate, pageable);
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

        List<String> warnings =new ArrayList<>();
        if (target == OrderStatus.RESERVED && order.getStatus() == OrderStatus.CREATED) {

            for (SalesOrderLine line : order.getLines()) {
                Long warehouseId = order.getWarehouse().getId();
                Long productId = line.getProduct().getId();
                String productName = line.getProduct().getName();
                //warning blasst exception
                Inventory inv = inventoryRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Pas d'inventaire pour product " + productName + " en warehouse " + warehouseId));
                int available = inv.getQtyOnHand() - inv.getQtyReserved();
                if (available < line.getQtyOrdered()) {
                    throw new BusinessException("Stock insuffisant pour product " + productId + ". Disponible: " + available + ", demandé: " + line.getQtyOrdered());
                }
            }


            for (SalesOrderLine line : order.getLines()) {
                Long warehouseId = order.getWarehouse().getId();
                Long productId = line.getProduct().getId();
                Inventory inv = inventoryRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId).get();
                inv.setQtyReserved(inv.getQtyReserved() + line.getQtyOrdered());
                inventoryRepository.save(inv);
                line.setQtyReserved(line.getQtyOrdered());
            }
        }

        // TODO: gérer d'autres transitions (ex: cancel -> dé-réserver)
        order.setStatus(target);
        SalesOrder saved = salesOrderRepository.save(order);
        log.info("SalesOrder id={} nouveau status={}", orderId, target);
        return salesOrderMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!salesOrderRepository.existsById(id)) {
            throw new ResourceNotFoundException("SalesOrder non trouvée: " + id);
        }
        salesOrderRepository.deleteById(id);
        log.info("SalesOrder supprimée id={}", id);
    }
}