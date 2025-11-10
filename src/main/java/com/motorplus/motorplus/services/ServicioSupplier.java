package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.supplierDtos.SupplierCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartCreateDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierPartPatchDto;
import com.motorplus.motorplus.dto.supplierDtos.SupplierUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServicioSupplier {
    Page<SupplierDto> list(String q, Pageable p);
    SupplierDto get(UUID id);
    SupplierDto create(SupplierCreateDto dto);
    SupplierDto update(UUID id, SupplierUpdateDto dto);
    void delete(UUID id);

    Page<SupplierPartDto> listSupplierParts(UUID supplierId, Pageable p);
    SupplierPartDto addSupplierPart(UUID supplierId, SupplierPartCreateDto dto);
    SupplierPartDto patchSupplierPart(UUID supplierId, UUID repuestoId, SupplierPartPatchDto dto);
    void removeSupplierPart(UUID supplierId, UUID repuestoId);
}
