package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.mechanicDtos.MechanicCreateDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicUpdateDto;
import com.motorplus.motorplus.services.MechanicService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/mechanics")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class MechanicController {

    private final MechanicService servicioMecanico;

    public MechanicController(MechanicService servicioMecanico) {
        this.servicioMecanico = servicioMecanico;
    }

    @GetMapping
    public Page<MechanicDto> list(@RequestParam(value = "q", required = false) String q,
                                  @RequestParam(value = "specialization", required = false) String specialization,
                                  @PageableDefault(size = 20) Pageable pageable) {
        return servicioMecanico.list(q, specialization, pageable);
    }

    @GetMapping("/{id}")
    public MechanicDto get(@PathVariable("id") UUID id) {
        return servicioMecanico.get(id);
    }

    @PostMapping
    public ResponseEntity<MechanicDto> create(@Valid @RequestBody MechanicCreateDto dto) {
        MechanicDto created = servicioMecanico.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public MechanicDto update(@PathVariable("id") UUID id, @Valid @RequestBody MechanicUpdateDto dto) {
        return servicioMecanico.update(id, dto);
    }

    @PatchMapping("/{id}/active")
    public MechanicDto setActive(@PathVariable("id") UUID id, @RequestParam(value = "active") boolean active) {
        return servicioMecanico.setActive(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        servicioMecanico.delete(id);
        return ResponseEntity.noContent().build();
    }
}
