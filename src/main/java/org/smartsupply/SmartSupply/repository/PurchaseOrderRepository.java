package org.smartsupply.SmartSupply.repository;

import org.smartsupply.SmartSupply.model.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    long countBySupplierId(Long supplierId);
}