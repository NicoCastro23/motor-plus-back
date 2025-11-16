package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.assigmentDto.AssignmentCreateDto;
import com.motorplus.motorplus.dto.assigmentDto.AssignmentDto;
import com.motorplus.motorplus.dto.assigmentDto.AssignmentPatchDto;
import com.motorplus.motorplus.dto.itemPartDtos.ItemPartCreateDto;
import com.motorplus.motorplus.dto.itemPartDtos.ItemPartDto;
import com.motorplus.motorplus.dto.itemPartDtos.ItemPartPatchDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderCreateDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderFilter;
import com.motorplus.motorplus.dto.ordersDtos.OrderItemCreateDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderItemDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderItemPatchDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderPatchDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderStatus;
import com.motorplus.motorplus.services.ServicioOrden;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class OrderController {

    private final ServicioOrden servicioOrden;

    public OrderController(ServicioOrden servicioOrden) {
        this.servicioOrden = servicioOrden;
    }

    @GetMapping
    public Page<OrderDto> list(@ModelAttribute OrderFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return servicioOrden.list(filter, pageable);
    }

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable UUID id) {
        return servicioOrden.get(id);
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderCreateDto dto) {
        OrderDto created = servicioOrden.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public OrderDto patch(@PathVariable UUID id, @RequestBody OrderPatchDto dto) {
        return servicioOrden.patch(id, dto);
    }

    @PostMapping("/{id}/status")
    public OrderDto changeStatus(@PathVariable UUID id, @RequestParam OrderStatus status) {
        return servicioOrden.changeStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        servicioOrden.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/items")
    public Page<OrderItemDto> listItems(@PathVariable UUID id, @PageableDefault(size = 20) Pageable pageable) {
        return servicioOrden.listItems(id, pageable);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderItemDto> addItem(@PathVariable UUID id, @Valid @RequestBody OrderItemCreateDto dto) {
        OrderItemDto created = servicioOrden.addItem(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItemDto getItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return servicioOrden.getItem(orderId, itemId);
    }

    @PatchMapping("/{orderId}/items/{itemId}")
    public OrderItemDto patchItem(@PathVariable UUID orderId, @PathVariable UUID itemId, @RequestBody OrderItemPatchDto dto) {
        return servicioOrden.patchItem(orderId, itemId, dto);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        servicioOrden.removeItem(orderId, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}/items/{itemId}/assignments")
    public Page<AssignmentDto> listAssignments(@PathVariable UUID orderId, @PathVariable UUID itemId, @PageableDefault(size = 20) Pageable pageable) {
        return servicioOrden.listAssignments(orderId, itemId, pageable);
    }

    @PostMapping("/{orderId}/items/{itemId}/assignments")
    public ResponseEntity<AssignmentDto> addAssignment(@PathVariable UUID orderId, @PathVariable UUID itemId, @Valid @RequestBody AssignmentCreateDto dto) {
        AssignmentDto created = servicioOrden.addAssignment(orderId, itemId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{orderId}/items/{itemId}/assignments/{mechanicId}")
    public AssignmentDto patchAssignment(@PathVariable UUID orderId, @PathVariable UUID itemId, @PathVariable UUID mechanicId, @RequestBody AssignmentPatchDto dto) {
        return servicioOrden.patchAssignment(orderId, itemId, mechanicId, dto);
    }

    @DeleteMapping("/{orderId}/items/{itemId}/assignments/{mechanicId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable UUID orderId, @PathVariable UUID itemId, @PathVariable UUID mechanicId) {
        servicioOrden.removeAssignment(orderId, itemId, mechanicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}/items/{itemId}/parts")
    public Page<ItemPartDto> listItemParts(@PathVariable UUID orderId, @PathVariable UUID itemId, @PageableDefault(size = 20) Pageable pageable) {
        return servicioOrden.listItemParts(orderId, itemId, pageable);
    }

    @PostMapping("/{orderId}/items/{itemId}/parts")
    public ResponseEntity<ItemPartDto> addItemPart(@PathVariable UUID orderId, @PathVariable UUID itemId, @Valid @RequestBody ItemPartCreateDto dto) {
        ItemPartDto created = servicioOrden.addItemPart(orderId, itemId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{orderId}/items/{itemId}/parts/{partId}")
    public ItemPartDto patchItemPart(@PathVariable UUID orderId, @PathVariable UUID itemId, @PathVariable UUID partId, @RequestBody ItemPartPatchDto dto) {
        return servicioOrden.patchItemPart(orderId, itemId, partId, dto);
    }

    @DeleteMapping("/{orderId}/items/{itemId}/parts/{partId}")
    public ResponseEntity<Void> removeItemPart(@PathVariable UUID orderId, @PathVariable UUID itemId, @PathVariable Long partId) {
        servicioOrden.removeItemPart(orderId, itemId, partId);
        return ResponseEntity.noContent().build();
    }
}
