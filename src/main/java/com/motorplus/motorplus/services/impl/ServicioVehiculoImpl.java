package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.ordersDtos.OrderDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleFilter;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleHistoryDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleUpdateDto;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.OrderMapper;
import com.motorplus.motorplus.mapper.ReportMapper;
import com.motorplus.motorplus.mapper.VehicleMapper;
import com.motorplus.motorplus.model.Order;
import com.motorplus.motorplus.model.Vehicle;
import com.motorplus.motorplus.services.ServicioVehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ServicioVehiculoImpl implements ServicioVehiculo {

    private final VehicleMapper vehicleMapper;
    private final OrderMapper orderMapper;
    private final ReportMapper reportMapper;

    public ServicioVehiculoImpl(VehicleMapper vehicleMapper, OrderMapper orderMapper, ReportMapper reportMapper) {
        this.vehicleMapper = vehicleMapper;
        this.orderMapper = orderMapper;
        this.reportMapper = reportMapper;
    }

    @Override
    public Page<VehicleDto> list(VehicleFilter f, Pageable pageable) {
        String brand = f != null ? f.brand() : null;
        String model = f != null ? f.model() : null;
        String plate = f != null ? f.licensePlate() : null;
        List<Vehicle> vehicles = vehicleMapper.findAll(brand, model, plate, pageable.getPageSize(), pageable.getOffset());
        long total = vehicleMapper.count(brand, model, plate);
        List<VehicleDto> content = vehicles.stream().map(this::toDto).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public VehicleDto get(String placa) {
        Vehicle vehicle = vehicleMapper.findByLicense(placa);
        if (vehicle == null) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        return toDto(vehicle);
    }

    @Override
    public VehicleDto create(VehicleCreateDto dto) {
        Vehicle existing = vehicleMapper.findByLicense(dto.licensePlate());
        if (existing != null) {
            throw new ResourceConflictException("Ya existe un vehículo con esa placa");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.randomUUID());
        vehicle.setClientId(null);
        vehicle.setBrand(dto.brand());
        vehicle.setModel(dto.model());
        vehicle.setLicensePlate(dto.licensePlate());
        vehicle.setModelYear(dto.modelYear());
        vehicle.setCreatedAt(Instant.now());
        vehicleMapper.insert(vehicle);
        return toDto(vehicle);
    }

    @Override
    public VehicleDto update(String placa, VehicleUpdateDto dto) {
        Vehicle vehicle = vehicleMapper.findByLicense(placa);
        if (vehicle == null) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        vehicle.setBrand(dto.brand());
        vehicle.setModel(dto.model());
        vehicle.setLicensePlate(dto.licensePlate());
        vehicle.setModelYear(dto.modelYear());
        vehicleMapper.update(vehicle);
        return toDto(vehicle);
    }

    @Override
    public void delete(String placa) {
        Vehicle vehicle = vehicleMapper.findByLicense(placa);
        if (vehicle == null) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        vehicleMapper.delete(placa);
    }

    @Override
    public Page<OrderDto> listOrders(String placa, Pageable pageable) {
        Vehicle vehicle = vehicleMapper.findByLicense(placa);
        if (vehicle == null) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        List<Order> orders = orderMapper.findAll(null, placa, null, null, null, pageable.getPageSize(), pageable.getOffset());
        long total = orderMapper.count(null, placa, null, null, null);
        List<OrderDto> content = orders.stream().map(this::toOrderDto).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public VehicleHistoryDto history(String placa) {
        Vehicle vehicle = vehicleMapper.findByLicense(placa);
        if (vehicle == null) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        List<Map<String, Object>> rows = reportMapper.vehicleHistory(placa);
        List<VehicleHistoryDto.VehicleHistoryEntry> entries = rows.stream()
                .map(row -> new VehicleHistoryDto.VehicleHistoryEntry(
                        (Instant) row.get("eventdate"),
                        row.get("description") != null ? row.get("description").toString() : null,
                        row.get("reference") != null ? row.get("reference").toString() : null
                ))
                .toList();
        return new VehicleHistoryDto(placa, entries);
    }

    private VehicleDto toDto(Vehicle vehicle) {
        return new VehicleDto(
            vehicle.getId(), 
            vehicle.getClientId(), 
            vehicle.getBrand(), 
            vehicle.getModel(), 
            vehicle.getLicensePlate(), 
            vehicle.getModelYear(), 
            vehicle.getCreatedAt(),
            vehicle.getClientName()
        );
    }

    private OrderDto toOrderDto(Order order) {
        return new OrderDto(order.getId(), order.getClientId(), order.getLicensePlate(), order.getStatus(), order.getDescription(), order.getTotal(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
