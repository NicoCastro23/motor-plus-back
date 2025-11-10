package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.domain.client.Client;
import com.motorplus.motorplus.domain.vehicle.Vehicle;
import com.motorplus.motorplus.dto.usersDtos.ClientCreateDto;
import com.motorplus.motorplus.dto.usersDtos.ClientDto;
import com.motorplus.motorplus.dto.usersDtos.ClientUpdateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mappers.ClientMapper;
import com.motorplus.motorplus.mappers.VehicleMapper;
import com.motorplus.motorplus.repository.ClientRepository;
import com.motorplus.motorplus.repository.VehicleRepository;
import com.motorplus.motorplus.services.ServicioCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Transactional
public class ServicioClienteImpl implements ServicioCliente {

    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final ClientMapper clientMapper;
    private final VehicleMapper vehicleMapper;

    public ServicioClienteImpl(ClientRepository clientRepository,
                               VehicleRepository vehicleRepository,
                               ClientMapper clientMapper,
                               VehicleMapper vehicleMapper) {
        this.clientRepository = clientRepository;
        this.vehicleRepository = vehicleRepository;
        this.clientMapper = clientMapper;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDto> list(String q, String email, Pageable pageable) {
        Specification<Client> specification = buildSpecification(q, email);
        return clientRepository.findAll(specification, pageable).map(clientMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDto get(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        return clientMapper.toDto(client);
    }

    @Override
    public ClientDto create(ClientCreateDto dto) {
        if (clientRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new ResourceConflictException("Ya existe un cliente con el correo electrónico proporcionado");
        }
        Client client = clientMapper.toEntity(dto);
        Client saved = clientRepository.save(client);
        return clientMapper.toDto(saved);
    }

    @Override
    public ClientDto update(UUID id, ClientUpdateDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        if (clientRepository.existsByEmailIgnoreCaseAndIdNot(dto.email(), id)) {
            throw new ResourceConflictException("Ya existe un cliente con el correo electrónico proporcionado");
        }
        clientMapper.updateEntity(dto, client);
        Client updated = clientRepository.save(client);
        return clientMapper.toDto(updated);
    }

    @Override
    public void delete(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        clientRepository.delete(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleDto> listVehicles(UUID clientId, Pageable pageable) {
        verifyClientExists(clientId);
        return vehicleRepository.findByClientId(clientId, pageable).map(vehicleMapper::toDto);
    }

    @Override
    public VehicleDto addVehicle(UUID clientId, VehicleCreateDto dto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        if (vehicleRepository.existsByClientIdAndLicensePlateIgnoreCase(clientId, dto.licensePlate())) {
            throw new ResourceConflictException("Ya existe un vehículo con la placa proporcionada para este cliente");
        }
        Vehicle vehicle = vehicleMapper.toEntity(dto, client);
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toDto(saved);
    }

    private Specification<Client> buildSpecification(String q, String email) {
        Specification<Client> specification = Specification.where(null);
        if (StringUtils.hasText(q)) {
            String likeValue = "%" + q.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("firstName")), likeValue),
                    builder.like(builder.lower(root.get("lastName")), likeValue),
                    builder.like(builder.lower(root.get("email")), likeValue)
            ));
        }
        if (StringUtils.hasText(email)) {
            String normalizedEmail = email.trim().toLowerCase();
            specification = specification.and((root, query, builder) -> builder.equal(builder.lower(root.get("email")), normalizedEmail));
        }
        return specification;
    }

    private void verifyClientExists(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
    }
}
