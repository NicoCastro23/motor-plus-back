package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.mechanicDtos.MechanicCreateDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServicioMecanico {
    Page<MechanicDto> list(String q, String especializacion, Pageable p);
    MechanicDto get(UUID id);
    MechanicDto create(MechanicCreateDto dto);
    MechanicDto update(UUID id, MechanicUpdateDto dto);
    void delete(UUID id);
}
