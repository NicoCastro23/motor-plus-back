package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.usersDtos.ClientCreateDto;
import com.motorplus.motorplus.dto.usersDtos.ClientDto;
import com.motorplus.motorplus.dto.usersDtos.ClientUpdateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServicioCliente {
    Page<ClientDto> list(String q, String email, Pageable pageable);
    ClientDto get(UUID id);
    ClientDto create(ClientCreateDto dto);
    ClientDto update(UUID id, ClientUpdateDto dto);
    void delete(UUID id);
    Page<VehicleDto> listVehicles(UUID clientId, Pageable pageable);
    VehicleDto addVehicle(UUID clientId, VehicleCreateDto dto);
}
