package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.mechanicDtos.MechanicCreateDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicDto;
import com.motorplus.motorplus.dto.mechanicDtos.MechanicUpdateDto;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.MechanicMapper;
import com.motorplus.motorplus.model.Mechanic;
import com.motorplus.motorplus.services.ServicioMecanico;
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
public class ServicioMecanicoImpl implements ServicioMecanico {

    private final MechanicMapper mechanicMapper;

    public ServicioMecanicoImpl(MechanicMapper mechanicMapper) {
        this.mechanicMapper = mechanicMapper;
    }

    @Override
    public Page<MechanicDto> list(String q, String especializacion, Pageable p) {
        List<Mechanic> mechanics = mechanicMapper.findAll(q, especializacion, p.getPageSize(), p.getOffset());
        List<MechanicDto> content = mechanics.stream().map(this::toDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public MechanicDto get(UUID id) {
        Mechanic mechanic = mechanicMapper.findById(id);
        if (mechanic == null) {
            throw new ResourceNotFoundException("Mecánico no encontrado");
        }
        return toDto(mechanic);
    }

    @Override
    public MechanicDto create(MechanicCreateDto dto) {
        Mechanic mechanic = new Mechanic();
        mechanic.setId(UUID.randomUUID());
        mechanic.setFirstName(dto.firstName());
        mechanic.setLastName(dto.lastName());
        mechanic.setSpecialization(dto.specialization());
        mechanic.setPhone(dto.phone());
        mechanic.setActive(true);
        mechanic.setCreatedAt(Instant.now());
        mechanicMapper.insert(mechanic);
        return toDto(mechanic);
    }

    @Override
    public MechanicDto update(UUID id, MechanicUpdateDto dto) {
        Mechanic mechanic = mechanicMapper.findById(id);
        if (mechanic == null) {
            throw new ResourceNotFoundException("Mecánico no encontrado");
        }
        mechanic.setFirstName(dto.firstName());
        mechanic.setLastName(dto.lastName());
        mechanic.setSpecialization(dto.specialization());
        if (dto.phone() != null) {
            mechanic.setPhone(dto.phone());
        }
        if (dto.active() != null) {
            mechanic.setActive(dto.active());
        }
        mechanicMapper.update(mechanic);
        return toDto(mechanic);
    }

    @Override
    public void delete(UUID id) {
        Mechanic mechanic = mechanicMapper.findById(id);
        if (mechanic == null) {
            throw new ResourceNotFoundException("Mecánico no encontrado");
        }
        mechanicMapper.delete(id);
    }

    private MechanicDto toDto(Mechanic mechanic) {
        return new MechanicDto(mechanic.getId(), mechanic.getFirstName(), mechanic.getLastName(), mechanic.getSpecialization(), mechanic.getPhone(), mechanic.isActive(), mechanic.getCreatedAt());
    }
}
