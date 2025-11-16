package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.mechanicDtos.MechanicCreateDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicUpdateDto;
import com.motorplus.motorplus.services.ServicioMecanico;
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

    private final ServicioMecanico servicioMecanico;

    public MechanicController(ServicioMecanico servicioMecanico) {
        this.servicioMecanico = servicioMecanico;
    }

    @GetMapping
    public Page<MechanicDto> list(@RequestParam(required = false) String q,
                                  @RequestParam(required = false) String specialization,
                                  @PageableDefault(size = 20) Pageable pageable) {
        return servicioMecanico.list(q, specialization, pageable);
    }

    @GetMapping("/{id}")
    public MechanicDto get(@PathVariable UUID id) {
        return servicioMecanico.get(id);
    }

    @PostMapping
    public ResponseEntity<MechanicDto> create(@Valid @RequestBody MechanicCreateDto dto) {
        MechanicDto created = servicioMecanico.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public MechanicDto update(@PathVariable UUID id, @Valid @RequestBody MechanicUpdateDto dto) {
        return servicioMecanico.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        servicioMecanico.delete(id);
        return ResponseEntity.noContent().build();
    }
}
