package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.movementDtos.MovementCreateDto;
import com.motorplus.motorplus.dto.movementDtos.MovementDto;
import com.motorplus.motorplus.dto.movementDtos.MovementFilter;
import com.motorplus.motorplus.dto.movementDtos.MovementType;
import com.motorplus.motorplus.dto.partDtos.PartCreateDto;
import com.motorplus.motorplus.dto.partDtos.PartDto;
import com.motorplus.motorplus.dto.partDtos.PartFilter;
import com.motorplus.motorplus.dto.partDtos.PartUpdateDto;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.MovementMapper;
import com.motorplus.motorplus.mapper.PartMapper;
import com.motorplus.motorplus.model.Movement;
import com.motorplus.motorplus.model.Part;
import com.motorplus.motorplus.services.ServicioPart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ServicioPartImpl implements ServicioPart {

    private final PartMapper partMapper;
    private final MovementMapper movementMapper;

    public ServicioPartImpl(PartMapper partMapper, MovementMapper movementMapper) {
        this.partMapper = partMapper;
        this.movementMapper = movementMapper;
    }

    @Override
    public Page<PartDto> list(PartFilter f, Pageable p) {
        String query = f != null ? f.q() : null;
        Boolean active = f != null ? f.active() : null;
        List<Part> parts = partMapper.findAll(query, active, p.getPageSize(), p.getOffset());
        long total = partMapper.count(query, active);
        List<PartDto> content = parts.stream().map(this::toDto).toList();
        return new PageImpl<>(content, p, total);
    }

    @Override
    public PartDto get(UUID id) {
        Part part = partMapper.findById(id);
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        return toDto(part);
    }

    @Override
    public PartDto create(PartCreateDto dto) {
        Part part = new Part();
        part.setId(UUID.randomUUID());
        part.setName(dto.name());
        part.setSku(dto.sku());
        part.setDescription(dto.description());
        part.setUnitPrice(dto.unitPrice());
        part.setStock(dto.stock());
        part.setActive(true);
        part.setCreatedAt(Instant.now());
        partMapper.insert(part);
        return toDto(part);
    }

    @Override
    public PartDto update(UUID id, PartUpdateDto dto) {
        Part part = partMapper.findById(id);
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        if (dto.name() != null) part.setName(dto.name());
        if (dto.sku() != null) part.setSku(dto.sku());
        if (dto.description() != null) part.setDescription(dto.description());
        if (dto.unitPrice() != null) part.setUnitPrice(dto.unitPrice());
        if (dto.stock() != null) part.setStock(dto.stock());
        if (dto.active() != null) part.setActive(dto.active());
        partMapper.update(part);
        return toDto(part);
    }

    @Override
    public PartDto setActive(UUID id, boolean active) {
        Part part = partMapper.findById(id);
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        part.setActive(active);
        partMapper.update(part);
        return toDto(part);
    }

    @Override
    public void delete(UUID id) {
        Part part = partMapper.findById(id);
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        partMapper.delete(id);
    }

    @Override
    public Page<MovementDto> listMovements(UUID partId, MovementFilter f, Pageable p) {
        Part part = partMapper.findById(partId);
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        MovementType type = f != null ? f.type() : null;
        Instant from = f != null ? f.from() : null;
        Instant to = f != null ? f.to() : null;
        List<Movement> movements = movementMapper.findByPart(partId, type, from, to, p.getPageSize(), p.getOffset());
        List<MovementDto> content = movements.stream().map(this::toMovementDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public MovementDto createMovement(UUID partId, MovementCreateDto dto) {
        Part part = partMapper.findById(partId);
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        int delta = dto.type() == MovementType.IN ? dto.quantity() : -dto.quantity();
        if (dto.type() == MovementType.OUT && part.getStock() < dto.quantity()) {
            throw new ResourceConflictException("Stock insuficiente");
        }
        Movement movement = new Movement();
        movement.setId(UUID.randomUUID());
        movement.setPartId(partId);
        movement.setType(dto.type());
        movement.setQuantity(dto.quantity());
        movement.setPerformedAt(Instant.now());
        movement.setNotes(dto.notes());
        movementMapper.insert(movement);
        partMapper.updateStock(partId, delta);
        part.setStock(part.getStock() + delta);
        return toMovementDto(movement);
    }

    private PartDto toDto(Part part) {
        return new PartDto(part.getId(), part.getName(), part.getSku(), part.getDescription(), part.getUnitPrice(), part.getStock(), part.isActive(), part.getCreatedAt());
    }

    private MovementDto toMovementDto(Movement movement) {
        return new MovementDto(movement.getId(), movement.getPartId(), movement.getType(), movement.getQuantity(), movement.getPerformedAt(), movement.getNotes());
    }
}
