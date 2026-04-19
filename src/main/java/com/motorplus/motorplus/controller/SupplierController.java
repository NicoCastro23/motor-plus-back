package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.supplierDtos.SupplierCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartPatchDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierUpdateDto;
import com.motorplus.motorplus.services.SupplierService;
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

    private final SupplierService servicioSupplier;

    public SupplierController(SupplierService servicioSupplier) {
        this.servicioSupplier = servicioSupplier;
    }

    @GetMapping
    public Page<SupplierDto> list(@RequestParam(value = "q", required = false) String q,
                                  @PageableDefault(size = 20) Pageable pageable) {
        return servicioSupplier.list(q, pageable);
    }

    @GetMapping("/{id}")
    public SupplierDto get(@PathVariable("id") UUID id) {
        return servicioSupplier.get(id);
    }

    @PostMapping
    public ResponseEntity<SupplierDto> create(@Valid @RequestBody SupplierCreateDto dto) {
        SupplierDto created = servicioSupplier.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public SupplierDto update(@PathVariable("id") UUID id, @Valid @RequestBody SupplierUpdateDto dto) {
        return servicioSupplier.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        servicioSupplier.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/parts")
    public Page<SupplierPartDto> listParts(@PathVariable("id") UUID id, @PageableDefault(size = 20) Pageable pageable) {
        return servicioSupplier.listSupplierParts(id, pageable);
    }

    @PostMapping("/{id}/parts")
    public ResponseEntity<SupplierPartDto> addPart(@PathVariable("id") UUID id, @Valid @RequestBody SupplierPartCreateDto dto) {
        SupplierPartDto created = servicioSupplier.addSupplierPart(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/parts/{partId}")
    public SupplierPartDto patchPart(@PathVariable("id") UUID id, @PathVariable("partId") UUID partId, @RequestBody SupplierPartPatchDto dto) {
        return servicioSupplier.patchSupplierPart(id, partId, dto);
    }

    @DeleteMapping("/{id}/parts/{partId}")
    public ResponseEntity<Void> removePart(@PathVariable("id") UUID id, @PathVariable("partId") UUID partId) {
        servicioSupplier.removeSupplierPart(id, partId);
        return ResponseEntity.noContent().build();
    }
}
