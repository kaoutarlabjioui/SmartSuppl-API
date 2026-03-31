package org.smartsupply.repository;

import org.smartsupply.model.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findBySalesOrderId(Long salesOrderId);

    boolean existsBySalesOrderId(Long salesOrderId);
}
