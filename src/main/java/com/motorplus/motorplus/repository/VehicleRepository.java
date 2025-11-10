package com.motorplus.motorplus.repository;

import com.motorplus.motorplus.domain.vehicle.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Page<Vehicle> findByClientId(UUID clientId, Pageable pageable);

    boolean existsByClientIdAndLicensePlateIgnoreCase(UUID clientId, String licensePlate);
}
