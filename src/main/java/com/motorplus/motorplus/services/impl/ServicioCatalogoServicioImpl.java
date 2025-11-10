package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.serviceDtos.ServiceCreateDto;
import com.motorplus.motorplus.dto.serviceDtos.ServiceDto;
import com.motorplus.motorplus.dto.serviceDtos.ServiceUpdateDto;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.ServiceCatalogMapper;
import com.motorplus.motorplus.model.ServiceCatalog;
import com.motorplus.motorplus.services.ServicioCatalogoServicio;
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
public class ServicioCatalogoServicioImpl implements ServicioCatalogoServicio {

    private final ServiceCatalogMapper serviceCatalogMapper;

    public ServicioCatalogoServicioImpl(ServiceCatalogMapper serviceCatalogMapper) {
        this.serviceCatalogMapper = serviceCatalogMapper;
    }

    @Override
    public Page<ServiceDto> list(String q, Boolean activo, Pageable p) {
        List<ServiceCatalog> services = serviceCatalogMapper.findAll(q, activo, p.getPageSize(), p.getOffset());
        long total = serviceCatalogMapper.count(q, activo);
        List<ServiceDto> content = services.stream().map(this::toDto).toList();
        return new PageImpl<>(content, p, total);
    }

    @Override
    public ServiceDto get(UUID id) {
        ServiceCatalog service = serviceCatalogMapper.findById(id);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio no encontrado");
        }
        return toDto(service);
    }

    @Override
    public ServiceDto create(ServiceCreateDto dto) {
        ServiceCatalog service = new ServiceCatalog();
        service.setId(UUID.randomUUID());
        service.setName(dto.name());
        service.setDescription(dto.description());
        service.setPrice(dto.price());
        service.setActive(true);
        service.setCreatedAt(Instant.now());
        serviceCatalogMapper.insert(service);
        return toDto(service);
    }

    @Override
    public ServiceDto update(UUID id, ServiceUpdateDto dto) {
        ServiceCatalog service = serviceCatalogMapper.findById(id);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio no encontrado");
        }
        service.setName(dto.name());
        service.setDescription(dto.description());
        if (dto.price() != null) {
            service.setPrice(dto.price());
        }
        if (dto.active() != null) {
            service.setActive(dto.active());
        }
        serviceCatalogMapper.update(service);
        return toDto(service);
    }

    @Override
    public ServiceDto setActive(UUID id, boolean active) {
        ServiceCatalog service = serviceCatalogMapper.findById(id);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio no encontrado");
        }
        service.setActive(active);
        serviceCatalogMapper.update(service);
        return toDto(service);
    }

    @Override
    public void delete(UUID id) {
        ServiceCatalog service = serviceCatalogMapper.findById(id);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio no encontrado");
        }
        serviceCatalogMapper.delete(id);
    }

    private ServiceDto toDto(ServiceCatalog service) {
        return new ServiceDto(service.getId(), service.getName(), service.getDescription(), service.getPrice(), service.isActive(), service.getCreatedAt());
    }
}
