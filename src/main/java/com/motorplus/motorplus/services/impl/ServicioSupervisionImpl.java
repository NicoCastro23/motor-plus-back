package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.supervisionDtos.SupervisionCreateDto;
import com.motorplus.motorplus.dto.supervisionDtos.SupervisionDto;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.SupervisionMapper;
import com.motorplus.motorplus.model.Supervision;
import com.motorplus.motorplus.services.ServicioSupervision;
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
public class ServicioSupervisionImpl implements ServicioSupervision {

    private final SupervisionMapper supervisionMapper;

    public ServicioSupervisionImpl(SupervisionMapper supervisionMapper) {
        this.supervisionMapper = supervisionMapper;
    }

    @Override
    public Page<SupervisionDto> list(UUID supervisorId, UUID supervisadoId, UUID ordenId, Pageable p) {
        List<Supervision> supervisions = supervisionMapper.findAll(supervisorId, supervisadoId, ordenId, p.getPageSize(), p.getOffset());
        List<SupervisionDto> content = supervisions.stream().map(this::toDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public SupervisionDto create(SupervisionCreateDto dto) {
        Supervision supervision = new Supervision();
        supervision.setSupervisorId(dto.supervisorId());
        supervision.setSupervisadoId(dto.supervisadoId());
        supervision.setOrderId(dto.orderId());
        supervision.setNotes(dto.notes());
        supervision.setCreatedAt(Instant.now());
        supervisionMapper.insert(supervision);
        return toDto(supervision);
    }

    @Override
    public void delete(UUID supervisorId, UUID supervisadoId, UUID ordenId) {
        Supervision supervision = supervisionMapper.find(supervisorId, supervisadoId, ordenId);
        if (supervision == null) {
            throw new ResourceNotFoundException("Supervisión no encontrada");
        }
        supervisionMapper.delete(supervisorId, supervisadoId, ordenId);
    }

    private SupervisionDto toDto(Supervision supervision) {
        return new SupervisionDto(supervision.getSupervisorId(), supervision.getSupervisadoId(), supervision.getOrderId(), supervision.getCreatedAt(), supervision.getNotes());
    }
}
