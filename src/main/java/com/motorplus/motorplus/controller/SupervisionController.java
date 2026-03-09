package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.supervisionDtos.SupervisionCreateDto;
import com.motorplus.motorplus.dto.supervisionDtos.SupervisionDto;
import com.motorplus.motorplus.services.SupervisionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/supervisions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class SupervisionController {

    private final SupervisionService servicioSupervision;

    public SupervisionController(SupervisionService servicioSupervision) {
        this.servicioSupervision = servicioSupervision;
    }

    @GetMapping
    public Page<SupervisionDto> list(@RequestParam(value = "supervisorId", required = false) UUID supervisorId,
                                     @RequestParam(value = "supervisadoId", required = false) UUID supervisadoId,
                                     @RequestParam(value = "orderId", required = false) UUID orderId,
                                     @PageableDefault(size = 20) Pageable pageable) {
        return servicioSupervision.list(supervisorId, supervisadoId, orderId, pageable);
    }

    @PostMapping
    public ResponseEntity<SupervisionDto> create(@Valid @RequestBody SupervisionCreateDto dto) {
        SupervisionDto created = servicioSupervision.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam(value = "supervisorId") UUID supervisorId,
                                        @RequestParam(value = "supervisadoId") UUID supervisadoId,
                                        @RequestParam(value = "orderId") UUID orderId) {
        servicioSupervision.delete(supervisorId, supervisadoId, orderId);
        return ResponseEntity.noContent().build();
    }
}
