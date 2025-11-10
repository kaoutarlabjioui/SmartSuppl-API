package org.smartsupply.SmartSupply.repository;

import org.smartsupply.SmartSupply.model.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier,Long> {
    boolean existsByName(String name);

    Optional<Carrier> findById(Long id);
    Optional<Carrier> findByName(String name);
    Optional<Carrier> findByEmail(String email);
}
