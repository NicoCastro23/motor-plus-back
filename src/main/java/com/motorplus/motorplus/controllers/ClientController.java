package com.motorplus.motorplus.controllers;

import com.motorplus.motorplus.dto.usersDtos.ClientCreateDto;
import com.motorplus.motorplus.dto.usersDtos.ClientDto;
import com.motorplus.motorplus.dto.usersDtos.ClientUpdateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import com.motorplus.motorplus.services.ServicioCliente;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@Validated
public class ClientController {

    private final ServicioCliente servicioCliente;

    public ClientController(ServicioCliente servicioCliente) {
        this.servicioCliente = servicioCliente;
    }

    @GetMapping
    public Page<ClientDto> listClients(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "email", required = false) String email,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return servicioCliente.list(q, email, pageable);
    }

    @GetMapping("/{id}")
    public ClientDto getClient(@PathVariable UUID id) {
        return servicioCliente.get(id);
    }

    @PostMapping
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientCreateDto dto) {
        ClientDto created = servicioCliente.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ClientDto updateClient(@PathVariable UUID id, @Valid @RequestBody ClientUpdateDto dto) {
        return servicioCliente.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        servicioCliente.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vehicles")
    public Page<VehicleDto> listClientVehicles(@PathVariable UUID id, @PageableDefault(size = 20) Pageable pageable) {
        return servicioCliente.listVehicles(id, pageable);
    }

    @PostMapping("/{id}/vehicles")
    public ResponseEntity<VehicleDto> addVehicle(@PathVariable UUID id, @Valid @RequestBody VehicleCreateDto dto) {
        VehicleDto created = servicioCliente.addVehicle(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
