package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.serviceDtos.ServiceCreateDto;
import com.motorplus.motorplus.dto.serviceDtos.ServiceDto;
import com.motorplus.motorplus.dto.serviceDtos.ServiceUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServicioCatalogoServicio {
    Page<ServiceDto> list(String q, Boolean activo, Pageable p);
    ServiceDto get(UUID id);
    ServiceDto create(ServiceCreateDto dto);
    ServiceDto update(UUID id, ServiceUpdateDto dto);
    ServiceDto setActive(UUID id, boolean active);
    void delete(UUID id);
}
