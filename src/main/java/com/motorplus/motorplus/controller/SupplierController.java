package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.supplierDtos.SupplierCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartPatchDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierUpdateDto;
import com.motorplus.motorplus.services.ServicioSupplier;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class SupplierController {

    private final ServicioSupplier servicioSupplier;

    public SupplierController(ServicioSupplier servicioSupplier) {
        this.servicioSupplier = servicioSupplier;
    }

    @GetMapping
    public Page<SupplierDto> list(@RequestParam(required = false) String q,
                                  @PageableDefault(size = 20) Pageable pageable) {
        return servicioSupplier.list(q, pageable);
    }

    @GetMapping("/{id}")
    public SupplierDto get(@PathVariable UUID id) {
        return servicioSupplier.get(id);
    }

    @PostMapping
    public ResponseEntity<SupplierDto> create(@Valid @RequestBody SupplierCreateDto dto) {
        SupplierDto created = servicioSupplier.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public SupplierDto update(@PathVariable UUID id, @Valid @RequestBody SupplierUpdateDto dto) {
        return servicioSupplier.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        servicioSupplier.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/parts")
    public Page<SupplierPartDto> listParts(@PathVariable UUID id, @PageableDefault(size = 20) Pageable pageable) {
        return servicioSupplier.listSupplierParts(id, pageable);
    }

    @PostMapping("/{id}/parts")
    public ResponseEntity<SupplierPartDto> addPart(@PathVariable UUID id, @Valid @RequestBody SupplierPartCreateDto dto) {
        SupplierPartDto created = servicioSupplier.addSupplierPart(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/parts/{partId}")
    public SupplierPartDto patchPart(@PathVariable UUID id, @PathVariable UUID partId, @RequestBody SupplierPartPatchDto dto) {
        return servicioSupplier.patchSupplierPart(id, partId, dto);
    }

    @DeleteMapping("/{id}/parts/{partId}")
    public ResponseEntity<Void> removePart(@PathVariable UUID id, @PathVariable UUID partId) {
        servicioSupplier.removeSupplierPart(id, partId);
        return ResponseEntity.noContent().build();
    }
}
