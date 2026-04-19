package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.stockAlertDtos.StockAlertDto;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.PartMapper;
import com.motorplus.motorplus.mapper.StockAlertMapper;
import com.motorplus.motorplus.model.Part;
import com.motorplus.motorplus.model.StockAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-alerts")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class StockAlertController {

    private final StockAlertMapper stockAlertMapper;
    private final PartMapper partMapper;

    public StockAlertController(StockAlertMapper stockAlertMapper, PartMapper partMapper) {
        this.stockAlertMapper = stockAlertMapper;
        this.partMapper = partMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<StockAlertDto> list(@RequestParam(required = false) Boolean resolved,
                                    @PageableDefault(size = 20) Pageable pageable) {
        List<StockAlert> alerts = stockAlertMapper.findAll(resolved, pageable.getPageSize(), pageable.getOffset());
        long total = stockAlertMapper.count(resolved);
        List<StockAlertDto> content = alerts.stream().map(this::toDto).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @PatchMapping("/{id}/resolve")
    @Transactional
    public ResponseEntity<StockAlertDto> resolve(@PathVariable("id") UUID id) {
        StockAlert alert = stockAlertMapper.findById(id);
        if (alert == null) {
            throw new ResourceNotFoundException("Alerta no encontrada");
        }
        stockAlertMapper.resolve(id, Instant.now());
        alert.setResolved(true);
        alert.setResolvedAt(Instant.now());
        return ResponseEntity.ok(toDto(alert));
    }

    private StockAlertDto toDto(StockAlert alert) {
        Part part = partMapper.findById(alert.getPartId());
        String partName = part != null ? part.getName() : null;
        String partSku = part != null ? part.getSku() : null;
        return new StockAlertDto(alert.getId(), alert.getPartId(), partName, partSku,
                alert.getCurrentStock(), alert.getMinStock(), alert.getCreatedAt(),
                alert.isResolved(), alert.getResolvedAt());
    }
}
