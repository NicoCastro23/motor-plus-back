package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.movementDtos.MovementCreateDto;
import com.motorplus.motorplus.dto.movementDtos.MovementDto;
import com.motorplus.motorplus.dto.movementDtos.MovementFilter;
import com.motorplus.motorplus.dto.partDtos.PartCreateDto;
import com.motorplus.motorplus.dto.partDtos.PartDto;
import com.motorplus.motorplus.dto.partDtos.PartFilter;
import com.motorplus.motorplus.dto.partDtos.PartUpdateDto;
import com.motorplus.motorplus.services.ServicioPart;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/parts")
public class PartController {

    private final ServicioPart servicioPart;

    public PartController(ServicioPart servicioPart) {
        this.servicioPart = servicioPart;
    }

    @GetMapping
    public Page<PartDto> list(@ModelAttribute PartFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return servicioPart.list(filter, pageable);
    }

    @GetMapping("/{id}")
    public PartDto get(@PathVariable UUID id) {
        return servicioPart.get(id);
    }

    @PostMapping
    public ResponseEntity<PartDto> create(@Valid @RequestBody PartCreateDto dto) {
        PartDto created = servicioPart.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public PartDto update(@PathVariable UUID id, @Valid @RequestBody PartUpdateDto dto) {
        return servicioPart.update(id, dto);
    }

    @PatchMapping("/{id}/active")
    public PartDto setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return servicioPart.setActive(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        servicioPart.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/movements")
    public Page<MovementDto> listMovements(@PathVariable UUID id, @ModelAttribute MovementFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return servicioPart.listMovements(id, filter, pageable);
    }

    @PostMapping("/{id}/movements")
    public ResponseEntity<MovementDto> createMovement(@PathVariable UUID id, @Valid @RequestBody MovementCreateDto dto) {
        MovementDto movement = servicioPart.createMovement(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }
}
