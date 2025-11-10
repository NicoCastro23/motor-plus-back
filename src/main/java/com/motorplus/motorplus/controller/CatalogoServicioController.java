package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.serviceDtos.ServiceCreateDto;
import com.motorplus.motorplus.dto.serviceDtos.ServiceDto;
import com.motorplus.motorplus.dto.serviceDtos.ServiceUpdateDto;
import com.motorplus.motorplus.services.ServicioCatalogoServicio;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class CatalogoServicioController {

    private final ServicioCatalogoServicio servicioCatalogoServicio;

    public CatalogoServicioController(ServicioCatalogoServicio servicioCatalogoServicio) {
        this.servicioCatalogoServicio = servicioCatalogoServicio;
    }

    @GetMapping
    public Page<ServiceDto> list(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) Boolean active,
                                 @PageableDefault(size = 20) Pageable pageable) {
        return servicioCatalogoServicio.list(q, active, pageable);
    }

    @GetMapping("/{id}")
    public ServiceDto get(@PathVariable UUID id) {
        return servicioCatalogoServicio.get(id);
    }

    @PostMapping
    public ResponseEntity<ServiceDto> create(@Valid @RequestBody ServiceCreateDto dto) {
        ServiceDto created = servicioCatalogoServicio.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ServiceDto update(@PathVariable UUID id, @Valid @RequestBody ServiceUpdateDto dto) {
        return servicioCatalogoServicio.update(id, dto);
    }

    @PatchMapping("/{id}/active")
    public ServiceDto setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return servicioCatalogoServicio.setActive(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        servicioCatalogoServicio.delete(id);
        return ResponseEntity.noContent().build();
    }
}
