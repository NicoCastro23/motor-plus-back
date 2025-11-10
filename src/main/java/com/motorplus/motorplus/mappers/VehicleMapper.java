package com.motorplus.motorplus.mappers;

import com.motorplus.motorplus.domain.client.Client;
import com.motorplus.motorplus.domain.vehicle.Vehicle;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleDto toDto(Vehicle vehicle) {
        return new VehicleDto(
                vehicle.getId(),
                vehicle.getClient().getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getLicensePlate(),
                vehicle.getModelYear(),
                vehicle.getCreatedAt()
        );
    }

    public Vehicle toEntity(VehicleCreateDto dto, Client client) {
        Vehicle vehicle = new Vehicle();
        vehicle.setClient(client);
        vehicle.setBrand(dto.brand());
        vehicle.setModel(dto.model());
        vehicle.setLicensePlate(dto.licensePlate());
        vehicle.setModelYear(dto.modelYear());
        return vehicle;
    }
}
