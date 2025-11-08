package org.smartsupply.SmartSupply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.smartsupply.SmartSupply.model.entity.SalesOrderLine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    List<SalesOrderLine> findBySalesOrderId(Long salesOrderId);
    List<SalesOrderLine> findByProductId(Long productId);
    long countByProductId(Long productId);
}
