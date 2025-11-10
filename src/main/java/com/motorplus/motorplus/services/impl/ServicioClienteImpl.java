package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.usersDtos.ClientCreateDto;
import com.motorplus.motorplus.dto.usersDtos.ClientDto;
import com.motorplus.motorplus.dto.usersDtos.ClientUpdateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.ClientMapper;
import com.motorplus.motorplus.mapper.VehicleMapper;
import com.motorplus.motorplus.model.Client;
import com.motorplus.motorplus.model.Vehicle;
import com.motorplus.motorplus.services.ServicioCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ServicioClienteImpl implements ServicioCliente {

    private final ClientMapper clientMapper;
    private final VehicleMapper vehicleMapper;

    public ServicioClienteImpl(ClientMapper clientMapper, VehicleMapper vehicleMapper) {
        this.clientMapper = clientMapper;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public Page<ClientDto> list(String q, String email, Pageable pageable) {
        List<Client> clients = clientMapper.findAll(q, email, pageable.getPageSize(), pageable.getOffset());
        long total = clientMapper.count(q, email);
        List<ClientDto> content = clients.stream().map(this::toDto).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public ClientDto get(UUID id) {
        Client client = clientMapper.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        return toDto(client);
    }

    @Override
    public ClientDto create(ClientCreateDto dto) {
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setFirstName(dto.firstName());
        client.setLastName(dto.lastName());
        client.setEmail(dto.email());
        client.setPhone(dto.phone());
        client.setCreatedAt(Instant.now());
        clientMapper.insert(client);
        return toDto(client);
    }

    @Override
    public ClientDto update(UUID id, ClientUpdateDto dto) {
        Client client = clientMapper.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        client.setFirstName(dto.firstName());
        client.setLastName(dto.lastName());
        client.setEmail(dto.email());
        client.setPhone(dto.phone());
        clientMapper.update(client);
        return toDto(client);
    }

    @Override
    public void delete(UUID id) {
        Client client = clientMapper.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        clientMapper.delete(id);
    }

    @Override
    public Page<VehicleDto> listVehicles(UUID clientId, Pageable pageable) {
        Client client = clientMapper.findById(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        List<Vehicle> vehicles = vehicleMapper.findByClient(clientId, pageable.getPageSize(), pageable.getOffset());
        long total = vehicleMapper.countByClient(clientId);
        List<VehicleDto> content = vehicles.stream().map(this::toVehicleDto).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public VehicleDto addVehicle(UUID clientId, VehicleCreateDto dto) {
        Client client = clientMapper.findById(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.randomUUID());
        vehicle.setClientId(clientId);
        vehicle.setBrand(dto.brand());
        vehicle.setModel(dto.model());
        vehicle.setLicensePlate(dto.licensePlate());
        vehicle.setModelYear(dto.modelYear());
        vehicle.setCreatedAt(Instant.now());
        vehicleMapper.insert(vehicle);
        return toVehicleDto(vehicle);
    }

    private ClientDto toDto(Client client) {
        return new ClientDto(client.getId(), client.getFirstName(), client.getLastName(), client.getEmail(), client.getPhone(), client.getCreatedAt());
    }

    private VehicleDto toVehicleDto(Vehicle vehicle) {
        return new VehicleDto(vehicle.getId(), vehicle.getClientId(), vehicle.getBrand(), vehicle.getModel(), vehicle.getLicensePlate(), vehicle.getModelYear(), vehicle.getCreatedAt());
    }
}
