package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.assigmentDto.AssignmentCreateDto;
import com.motorplus.motorplus.dto.assigmentDto.AssignmentDto;
import com.motorplus.motorplus.dto.assigmentDto.AssignmentPatchDto;
import com.motorplus.motorplus.dto.itemPartDtos.ItemPartCreateDto;
import com.motorplus.motorplus.dto.itemPartDtos.ItemPartDto;
import com.motorplus.motorplus.dto.itemPartDtos.ItemPartPatchDto;
import com.motorplus.motorplus.dto.ordersDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServicioOrden {
    Page<OrderDto> list(OrderFilter f, Pageable p);
    OrderDto get(UUID id);
    OrderDto create(OrderCreateDto dto);
    OrderDto patch(UUID id, OrderPatchDto dto);
    OrderDto changeStatus(UUID id, OrderStatus status);
    void delete(UUID id);

    // Ítems de servicio
    Page<OrderItemDto> listItems(UUID orderId, Pageable p);
    OrderItemDto addItem(UUID orderId, OrderItemCreateDto dto);
    OrderItemDto getItem(UUID orderId, UUID itemId);
    OrderItemDto patchItem(UUID orderId, UUID itemId, OrderItemPatchDto dto);
    void removeItem(UUID orderId, UUID itemId);

    // Asignaciones
    Page<AssignmentDto> listAssignments(UUID orderId, UUID itemId, Pageable p);
    AssignmentDto addAssignment(UUID orderId, UUID itemId, AssignmentCreateDto dto);
    AssignmentDto patchAssignment(UUID orderId, UUID itemId, UUID mecanicoId, AssignmentPatchDto dto);
    void removeAssignment(UUID orderId, UUID itemId, UUID mecanicoId);

    // Consumo de repuestos
    Page<ItemPartDto> listItemParts(UUID orderId, UUID itemId, Pageable p);
    ItemPartDto addItemPart(UUID orderId, UUID itemId, ItemPartCreateDto dto);
    ItemPartDto patchItemPart(UUID orderId, UUID itemId, UUID repuestoId, ItemPartPatchDto dto);
    void removeItemPart(UUID orderId, UUID itemId, Long repuestoId);
}
