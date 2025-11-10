package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.movementDtos.MovementCreateDto;
import com.motorplus.motorplus.dto.movementDtos.MovementDto;
import com.motorplus.motorplus.dto.movementDtos.MovementFilter;
import com.motorplus.motorplus.dto.partDtos.PartCreateDto;
import com.motorplus.motorplus.dto.partDtos.PartDto;
import com.motorplus.motorplus.dto.partDtos.PartFilter;
import com.motorplus.motorplus.dto.partDtos.PartUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServicioPart {
    Page<PartDto> list(PartFilter f, Pageable p);
    PartDto get(UUID id);
    PartDto create(PartCreateDto dto);
    PartDto update(UUID id, PartUpdateDto dto);
    PartDto setActive(UUID id, boolean active);
    void delete(UUID id);

    Page<MovementDto> listMovements(UUID partId, MovementFilter f, Pageable p);
    MovementDto createMovement(UUID partId, MovementCreateDto dto); // actualiza stock
}
