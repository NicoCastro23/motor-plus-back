package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.movementDtos.MovementCreateDto;
import com.motorplus.motorplus.dto.movementDtos.MovementDto;
import com.motorplus.motorplus.dto.movementDtos.MovementFilter;
import com.motorplus.motorplus.dto.partDtos.PartCreateDto;
import com.motorplus.motorplus.dto.partDtos.PartDto;
import com.motorplus.motorplus.dto.partDtos.PartFilter;
import com.motorplus.motorplus.dto.partDtos.PartUpdateDto;
import com.motorplus.motorplus.services.PartService;
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
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class PartController {

    private final PartService servicioPart;

    public PartController(PartService servicioPart) {
        this.servicioPart = servicioPart;
    }

    @GetMapping
    public Page<PartDto> list(@ModelAttribute PartFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return servicioPart.list(filter, pageable);
    }

    @GetMapping("/{id}")
    public PartDto get(@PathVariable(value = "id") UUID id) {
        return servicioPart.get(id);
    }

    @PostMapping
    public ResponseEntity<PartDto> create(@Valid @RequestBody PartCreateDto dto) {
        PartDto created = servicioPart.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public PartDto update(@PathVariable(value = "id") UUID id, @Valid @RequestBody PartUpdateDto dto) {
        return servicioPart.update(id, dto);
    }

    @PatchMapping("/{id}/active")
    public PartDto setActive(@PathVariable(value = "id") UUID id, @RequestParam(value = "active") boolean active) {
        return servicioPart.setActive(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") UUID id) {
        servicioPart.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/movements")
    public Page<MovementDto> listMovements(@PathVariable(value = "id") UUID id, @ModelAttribute MovementFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return servicioPart.listMovements(id, filter, pageable);
    }

    @PostMapping("/{id}/movements")
    public ResponseEntity<MovementDto> createMovement(@PathVariable(value = "id") UUID id, @Valid @RequestBody MovementCreateDto dto) {
        MovementDto movement = servicioPart.createMovement(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }
}
