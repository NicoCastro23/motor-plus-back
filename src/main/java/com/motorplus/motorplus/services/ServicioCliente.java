package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.usersDtos.ClientCreateDto;
import com.motorplus.motorplus.dto.usersDtos.ClientDto;
import com.motorplus.motorplus.dto.usersDtos.ClientUpdateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface ServicioCliente {
    Page<ClientDto> list(String q, String email, Pageable pageable);
    ClientDto get(Long id);
    ClientDto create(ClientCreateDto dto);
    ClientDto update(Long id, ClientUpdateDto dto);
    void delete(Long id);
    Page<VehicleDto> listVehicles(Long clientId, Pageable pageable);
    VehicleDto addVehicle(Long clientId, VehicleCreateDto dto);
}
