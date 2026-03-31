package org.smartsupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartsupply.model.entity.SalesOrder;
import org.smartsupply.model.enums.OrderStatus;
import org.smartsupply.model.enums.POStatus;
import org.smartsupply.repository.PurchaseOrderRepository;
import org.smartsupply.repository.SalesOrderRepository;
import org.smartsupply.service.SalesOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderService salesOrderService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Lancement du nettoyage des réservations expirées...");

        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<SalesOrder> expiredOrders = salesOrderRepository.findByStatusAndCreatedAtBefore(OrderStatus.RESERVED,
                threshold);

        for (SalesOrder order : expiredOrders) {
            log.info("Commande expirée détectée: ID={}. Libération du stock...", order.getId());

            try {
                // 1. Revert to CREATED to release stock
                salesOrderService.updateStatus(order.getId(), OrderStatus.CREATED.name());
                log.info("Commande {} repassée en CREATED. Stock libéré.", order.getId());

                // 2. Cancel associated Backorder PO if exists
                String ref = "SO:" + order.getId();
                purchaseOrderRepository.findByReference(ref).ifPresent(po -> {
                    if (po.getStatus() == POStatus.CREATED) {
                        log.info("Suppression du PurchaseOrder lié {} (Ref: {})", po.getId(), ref);
                        purchaseOrderRepository.delete(po);
                    } else {
                        log.warn("PurchaseOrder lié {} a déjà le statut {}. Pas de suppression.", po.getId(),
                                po.getStatus());
                    }
                });

            } catch (Exception e) {
                log.error("Erreur lors du nettoyage de la commande {}", order.getId(), e);
            }
        }
    }
}
