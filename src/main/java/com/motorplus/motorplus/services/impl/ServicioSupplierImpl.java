package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.supplierDtos.SupplierCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartPatchDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierUpdateDto;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.SupplierMapper;
import com.motorplus.motorplus.mapper.SupplierPartMapper;
import com.motorplus.motorplus.model.Supplier;
import com.motorplus.motorplus.model.SupplierPart;
import com.motorplus.motorplus.services.ServicioSupplier;
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
public class ServicioSupplierImpl implements ServicioSupplier {

    private final SupplierMapper supplierMapper;
    private final SupplierPartMapper supplierPartMapper;

    public ServicioSupplierImpl(SupplierMapper supplierMapper, SupplierPartMapper supplierPartMapper) {
        this.supplierMapper = supplierMapper;
        this.supplierPartMapper = supplierPartMapper;
    }

    @Override
    public Page<SupplierDto> list(String q, Pageable p) {
        List<Supplier> suppliers = supplierMapper.findAll(q, p.getPageSize(), p.getOffset());
        List<SupplierDto> content = suppliers.stream().map(this::toDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public SupplierDto get(UUID id) {
        Supplier supplier = supplierMapper.findById(id);
        if (supplier == null) {
            throw new ResourceNotFoundException("Proveedor no encontrado");
        }
        return toDto(supplier);
    }

    @Override
    public SupplierDto create(SupplierCreateDto dto) {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName(dto.name());
        supplier.setEmail(dto.email());
        supplier.setPhone(dto.phone());
        supplier.setActive(true);
        supplier.setCreatedAt(Instant.now());
        supplierMapper.insert(supplier);
        return toDto(supplier);
    }

    @Override
    public SupplierDto update(UUID id, SupplierUpdateDto dto) {
        Supplier supplier = supplierMapper.findById(id);
        if (supplier == null) {
            throw new ResourceNotFoundException("Proveedor no encontrado");
        }
        if (dto.name() != null) supplier.setName(dto.name());
        if (dto.email() != null) supplier.setEmail(dto.email());
        if (dto.phone() != null) supplier.setPhone(dto.phone());
        if (dto.active() != null) supplier.setActive(dto.active());
        supplierMapper.update(supplier);
        return toDto(supplier);
    }

    @Override
    public void delete(UUID id) {
        Supplier supplier = supplierMapper.findById(id);
        if (supplier == null) {
            throw new ResourceNotFoundException("Proveedor no encontrado");
        }
        supplierMapper.delete(id);
    }

    @Override
    public Page<SupplierPartDto> listSupplierParts(UUID supplierId, Pageable p) {
        Supplier supplier = supplierMapper.findById(supplierId);
        if (supplier == null) {
            throw new ResourceNotFoundException("Proveedor no encontrado");
        }
        List<SupplierPart> parts = supplierPartMapper.findBySupplier(supplierId, p.getPageSize(), p.getOffset());
        List<SupplierPartDto> content = parts.stream().map(this::toPartDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public SupplierPartDto addSupplierPart(UUID supplierId, SupplierPartCreateDto dto) {
        Supplier supplier = supplierMapper.findById(supplierId);
        if (supplier == null) {
            throw new ResourceNotFoundException("Proveedor no encontrado");
        }
        SupplierPart part = new SupplierPart();
        part.setSupplierId(supplierId);
        part.setPartId(dto.partId());
        part.setPrice(dto.price());
        part.setMinQuantity(dto.minQuantity());
        supplierPartMapper.insert(part);
        return toPartDto(part);
    }

    @Override
    public SupplierPartDto patchSupplierPart(UUID supplierId, UUID repuestoId, SupplierPartPatchDto dto) {
        SupplierPart part = supplierPartMapper.find(supplierId, repuestoId);
        if (part == null) {
            throw new ResourceNotFoundException("Relacion proveedor-repuesto no encontrada");
        }
        if (dto.price() != null) part.setPrice(dto.price());
        if (dto.minQuantity() != null) part.setMinQuantity(dto.minQuantity());
        supplierPartMapper.update(part);
        return toPartDto(part);
    }

    @Override
    public void removeSupplierPart(UUID supplierId, UUID repuestoId) {
        SupplierPart part = supplierPartMapper.find(supplierId, repuestoId);
        if (part == null) {
            throw new ResourceNotFoundException("Relacion proveedor-repuesto no encontrada");
        }
        supplierPartMapper.delete(supplierId, repuestoId);
    }

    private SupplierDto toDto(Supplier supplier) {
        return new SupplierDto(supplier.getId(), supplier.getName(), supplier.getEmail(), supplier.getPhone(), supplier.isActive(), supplier.getCreatedAt());
    }

    private SupplierPartDto toPartDto(SupplierPart part) {
        return new SupplierPartDto(part.getSupplierId(), part.getPartId(), part.getPrice(), part.getMinQuantity());
    }
}
