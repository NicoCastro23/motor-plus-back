package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.supervisionDtos.SupervisionCreateDto;
import com.motorplus.motorplus.dto.supervisionDtos.SupervisionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServicioSupervision {
    Page<SupervisionDto> list(UUID supervisorId, UUID supervisadoId, UUID ordenId, Pageable p);
    SupervisionDto create(SupervisionCreateDto dto);
    void delete(UUID supervisorId, UUID supervisadoId, UUID ordenId);
}
