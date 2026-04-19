package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.movementDtos.MovementType;
import com.motorplus.motorplus.dto.purchaseOrderDtos.*;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.MovementMapper;
import com.motorplus.motorplus.mapper.PartMapper;
import com.motorplus.motorplus.mapper.PurchaseOrderMapper;
import com.motorplus.motorplus.model.Movement;
import com.motorplus.motorplus.model.Part;
import com.motorplus.motorplus.model.PurchaseOrder;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchase-orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class PurchaseOrderController {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PartMapper partMapper;
    private final MovementMapper movementMapper;

    public PurchaseOrderController(PurchaseOrderMapper purchaseOrderMapper,
                                   PartMapper partMapper,
                                   MovementMapper movementMapper) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.partMapper = partMapper;
        this.movementMapper = movementMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto> list(@RequestParam(required = false) UUID partId,
                                       @RequestParam(required = false) PurchaseOrderStatus status,
                                       @PageableDefault(size = 20) Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        List<Map<String, Object>> rows = purchaseOrderMapper.findAllWithDetails(partId, statusStr, pageable.getPageSize(), pageable.getOffset());
        long total = purchaseOrderMapper.count(partId, statusStr);
        List<PurchaseOrderDto> content = rows.stream().map(this::rowToDto).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PurchaseOrderDto get(@PathVariable("id") UUID id) {
        Map<String, Object> row = purchaseOrderMapper.findByIdWithDetails(id);
        if (row == null) {
            throw new ResourceNotFoundException("Orden de compra no encontrada");
        }
        return rowToDto(row);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PurchaseOrderDto> create(@Valid @RequestBody PurchaseOrderCreateDto dto) {
        Part part = partMapper.findById(dto.partId());
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        PurchaseOrder order = new PurchaseOrder();
        order.setId(UUID.randomUUID());
        order.setPartId(dto.partId());
        order.setSupplierId(dto.supplierId());
        order.setQuantity(dto.quantity());
        order.setUnitCost(dto.unitCost());
        order.setStatus(PurchaseOrderStatus.PENDING.name());
        order.setNotes(dto.notes());
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        purchaseOrderMapper.insert(order);
        Map<String, Object> row = purchaseOrderMapper.findByIdWithDetails(order.getId());
        return ResponseEntity.status(201).body(rowToDto(row));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public PurchaseOrderDto updateStatus(@PathVariable("id") UUID id,
                                         @Valid @RequestBody PurchaseOrderUpdateStatusDto dto) {
        PurchaseOrder order = purchaseOrderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Orden de compra no encontrada");
        }
        PurchaseOrderStatus current = PurchaseOrderStatus.valueOf(order.getStatus());
        if (current == PurchaseOrderStatus.RECEIVED || current == PurchaseOrderStatus.CANCELLED) {
            throw new ResourceConflictException("No se puede cambiar el estado de una orden " + current.name());
        }

        order.setStatus(dto.status().name());
        if (dto.unitCost() != null) {
            order.setUnitCost(dto.unitCost());
        }
        order.setUpdatedAt(Instant.now());
        purchaseOrderMapper.update(order);

        if (dto.status() == PurchaseOrderStatus.RECEIVED) {
            partMapper.updateStock(order.getPartId(), order.getQuantity());
            Movement movement = new Movement();
            movement.setId(UUID.randomUUID());
            movement.setPartId(order.getPartId());
            movement.setType(MovementType.IN);
            movement.setQuantity(order.getQuantity());
            movement.setPerformedAt(Instant.now());
            movement.setNotes("Recepción de orden de compra " + id);
            movement.setSupplierId(order.getSupplierId());
            movement.setEntryCost(order.getUnitCost());
            movementMapper.insert(movement);
        }

        Map<String, Object> row = purchaseOrderMapper.findByIdWithDetails(id);
        return rowToDto(row);
    }

    @PostMapping("/{id}/return")
    @Transactional
    public ResponseEntity<Void> returnItems(@PathVariable("id") UUID id,
                                            @Valid @RequestBody PurchaseOrderReturnDto dto) {
        PurchaseOrder order = purchaseOrderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Orden de compra no encontrada");
        }
        if (!PurchaseOrderStatus.RECEIVED.name().equals(order.getStatus())) {
            throw new ResourceConflictException("Solo se pueden devolver artículos de órdenes en estado RECEIVED");
        }
        partMapper.updateStock(order.getPartId(), -dto.quantity());
        Movement movement = new Movement();
        movement.setId(UUID.randomUUID());
        movement.setPartId(order.getPartId());
        movement.setType(MovementType.OUT);
        movement.setQuantity(dto.quantity());
        movement.setPerformedAt(Instant.now());
        movement.setNotes("Devolución a proveedor" + (dto.notes() != null ? ": " + dto.notes() : "") + " (orden " + id + ")");
        movement.setSupplierId(order.getSupplierId());
        movement.setEntryCost(order.getUnitCost());
        movementMapper.insert(movement);
        return ResponseEntity.noContent().build();
    }

    private PurchaseOrderDto rowToDto(Map<String, Object> row) {
        UUID supplierId = row.get("supplierid") != null ? UUID.fromString(row.get("supplierid").toString()) : null;
        String supplierName = row.get("suppliername") != null ? row.get("suppliername").toString() : null;
        java.math.BigDecimal unitCost = row.get("unitcost") != null
                ? new java.math.BigDecimal(row.get("unitcost").toString()) : null;
        return new PurchaseOrderDto(
                UUID.fromString(row.get("id").toString()),
                UUID.fromString(row.get("partid").toString()),
                row.get("partname") != null ? row.get("partname").toString() : null,
                row.get("partsku") != null ? row.get("partsku").toString() : null,
                supplierId,
                supplierName,
                ((Number) row.get("quantity")).intValue(),
                unitCost,
                PurchaseOrderStatus.valueOf(row.get("status").toString()),
                row.get("notes") != null ? row.get("notes").toString() : null,
                row.get("createdat") != null ? ((java.sql.Timestamp) row.get("createdat")).toInstant() : null,
                row.get("updatedat") != null ? ((java.sql.Timestamp) row.get("updatedat")).toInstant() : null
        );
    }
}
