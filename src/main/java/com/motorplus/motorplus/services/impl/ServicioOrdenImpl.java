package com.motorplus.motorplus.services.impl;

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
import com.motorplus.motorplus.dto.movementDtos.MovementType;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.AssignmentMapper;
import com.motorplus.motorplus.mapper.ItemPartMapper;
import com.motorplus.motorplus.mapper.MovementMapper;
import com.motorplus.motorplus.mapper.OrderItemMapper;
import com.motorplus.motorplus.mapper.OrderMapper;
import com.motorplus.motorplus.mapper.PartMapper;
import com.motorplus.motorplus.mapper.VehicleMapper;
import com.motorplus.motorplus.model.Assignment;
import com.motorplus.motorplus.model.ItemPart;
import com.motorplus.motorplus.model.Movement;
import com.motorplus.motorplus.model.Order;
import com.motorplus.motorplus.model.OrderItem;
import com.motorplus.motorplus.model.Part;
import com.motorplus.motorplus.services.ServicioOrden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ServicioOrdenImpl implements ServicioOrden {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AssignmentMapper assignmentMapper;
    private final ItemPartMapper itemPartMapper;
    private final VehicleMapper vehicleMapper;
    private final PartMapper partMapper;
    private final MovementMapper movementMapper;

    public ServicioOrdenImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, AssignmentMapper assignmentMapper, ItemPartMapper itemPartMapper, VehicleMapper vehicleMapper, PartMapper partMapper, MovementMapper movementMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.assignmentMapper = assignmentMapper;
        this.itemPartMapper = itemPartMapper;
        this.vehicleMapper = vehicleMapper;
        this.partMapper = partMapper;
        this.movementMapper = movementMapper;
    }

    @Override
    public Page<OrderDto> list(OrderFilter f, Pageable p) {
        UUID clientId = f != null ? f.clientId() : null;
        String plate = f != null ? f.licensePlate() : null;
        OrderStatus status = f != null ? f.status() : null;
        Instant from = f != null ? f.from() : null;
        Instant to = f != null ? f.to() : null;
        List<Order> orders = orderMapper.findAll(clientId, plate, status, from, to, p.getPageSize(), p.getOffset());
        long total = orderMapper.count(clientId, plate, status, from, to);
        List<OrderDto> content = orders.stream().map(this::toDto).toList();
        return new PageImpl<>(content, p, total);
    }

    @Override
    public OrderDto get(UUID id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        return toDto(order);
    }

    @Override
    public OrderDto create(OrderCreateDto dto) {
        // Validar que el vehículo existe
        if (vehicleMapper.findByLicense(dto.licensePlate()) == null) {
            throw new ResourceNotFoundException("No existe un vehículo con la placa: " + dto.licensePlate());
        }
        
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setClientId(dto.clientId());
        order.setLicensePlate(dto.licensePlate());
        // Usar el estado del DTO si viene, o DRAFT por defecto
        order.setStatus(dto.status() != null ? dto.status() : OrderStatus.DRAFT);
        order.setDescription(dto.description());
        order.setTotal(BigDecimal.ZERO);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(order.getCreatedAt());
        orderMapper.insert(order);
        return toDto(order);
    }

    @Override
    public OrderDto patch(UUID id, OrderPatchDto dto) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        if (dto.description() != null) order.setDescription(dto.description());
        if (dto.licensePlate() != null) order.setLicensePlate(dto.licensePlate());
        if (dto.status() != null) order.setStatus(dto.status());
        order.setUpdatedAt(Instant.now());
        orderMapper.update(order);
        return toDto(order);
    }

    @Override
    public OrderDto changeStatus(UUID id, OrderStatus status) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        orderMapper.updateStatus(id, status, order.getUpdatedAt());
        return toDto(order);
    }

    @Override
    public void delete(UUID id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        orderMapper.delete(id);
    }

    @Override
    public Page<OrderItemDto> listItems(UUID orderId, Pageable p) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        List<OrderItem> items = orderItemMapper.findByOrder(orderId, p.getPageSize(), p.getOffset());
        List<OrderItemDto> content = items.stream().map(this::toItemDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public OrderItemDto addItem(UUID orderId, OrderItemCreateDto dto) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        OrderItem item = new OrderItem();
        item.setId(UUID.randomUUID());
        item.setOrderId(orderId);
        item.setServiceId(dto.serviceId());
        item.setDescription(dto.description());
        item.setQuantity(dto.quantity());
        item.setUnitPrice(dto.unitPrice());
        orderItemMapper.insert(item);
        recalculateTotal(orderId);
        return toItemDto(item);
    }

    @Override
    public OrderItemDto getItem(UUID orderId, UUID itemId) {
        OrderItem item = orderItemMapper.findById(orderId, itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado");
        }
        return toItemDto(item);
    }

    @Override
    public OrderItemDto patchItem(UUID orderId, UUID itemId, OrderItemPatchDto dto) {
        OrderItem item = orderItemMapper.findById(orderId, itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado");
        }
        if (dto.description() != null) item.setDescription(dto.description());
        if (dto.quantity() != null) item.setQuantity(dto.quantity());
        if (dto.unitPrice() != null) item.setUnitPrice(dto.unitPrice());
        orderItemMapper.update(item);
        recalculateTotal(orderId);
        return toItemDto(item);
    }

    @Override
    public void removeItem(UUID orderId, UUID itemId) {
        // Validar que la orden no esté completada
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ResourceConflictException("No se pueden eliminar items de una orden completada");
        }
        
        OrderItem item = orderItemMapper.findById(orderId, itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado");
        }
        
        // Restaurar stock de todos los repuestos asociados a este item
        List<ItemPart> parts = itemPartMapper.findByOrderItem(itemId, Integer.MAX_VALUE, 0);
        for (ItemPart part : parts) {
            // Restaurar stock
            partMapper.updateStock(part.getPartId(), part.getQuantity());
            
            // Crear movimiento de inventario (entrada - devolución)
            Movement movement = new Movement();
            movement.setId(UUID.randomUUID());
            movement.setPartId(part.getPartId());
            movement.setType(MovementType.IN);
            movement.setQuantity(part.getQuantity());
            movement.setPerformedAt(Instant.now());
            movement.setNotes("Devolución por eliminación de item en orden " + orderId + ", item " + itemId);
            movementMapper.insert(movement);
        }
        
        // Eliminar el item (esto eliminará automáticamente los repuestos asociados por CASCADE)
        orderItemMapper.delete(orderId, itemId);
        recalculateTotal(orderId);
    }

    @Override
    public Page<AssignmentDto> listAssignments(UUID orderId, UUID itemId, Pageable p) {
        OrderItem item = orderItemMapper.findById(orderId, itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado");
        }
        List<Assignment> assignments = assignmentMapper.findByOrderItem(itemId, p.getPageSize(), p.getOffset());
        List<AssignmentDto> content = assignments.stream().map(this::toAssignmentDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public AssignmentDto addAssignment(UUID orderId, UUID itemId, AssignmentCreateDto dto) {
        OrderItem item = orderItemMapper.findById(orderId, itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado");
        }
        Assignment assignment = new Assignment();
        assignment.setOrderItemId(itemId);
        assignment.setMechanicId(dto.mechanicId());
        assignment.setEstimatedHours(dto.estimatedHours());
        assignment.setAssignedAt(Instant.now());
        assignmentMapper.insert(assignment);
        return toAssignmentDto(assignment);
    }

    @Override
    public AssignmentDto patchAssignment(UUID orderId, UUID itemId, UUID mecanicoId, AssignmentPatchDto dto) {
        Assignment assignment = assignmentMapper.find(itemId, mecanicoId);
        if (assignment == null) {
            throw new ResourceNotFoundException("Asignación no encontrada");
        }
        if (dto.estimatedHours() != null) assignment.setEstimatedHours(dto.estimatedHours());
        assignmentMapper.update(assignment);
        return toAssignmentDto(assignment);
    }

    @Override
    public void removeAssignment(UUID orderId, UUID itemId, UUID mecanicoId) {
        Assignment assignment = assignmentMapper.find(itemId, mecanicoId);
        if (assignment == null) {
            throw new ResourceNotFoundException("Asignación no encontrada");
        }
        assignmentMapper.delete(itemId, mecanicoId);
    }

    @Override
    public Page<ItemPartDto> listItemParts(UUID orderId, UUID itemId, Pageable p) {
        OrderItem item = orderItemMapper.findById(orderId, itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado");
        }
        List<ItemPart> parts = itemPartMapper.findByOrderItem(itemId, p.getPageSize(), p.getOffset());
        List<ItemPartDto> content = parts.stream().map(this::toItemPartDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public ItemPartDto addItemPart(UUID orderId, UUID itemId, ItemPartCreateDto dto) {
        // Validar que la orden no esté completada
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ResourceConflictException("No se pueden agregar repuestos a una orden completada");
        }
        
        OrderItem item = orderItemMapper.findById(orderId, itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Item no encontrado");
        }
        
        // Validar que el repuesto existe y tiene stock suficiente
        Part part = partMapper.findById(dto.partId());
        if (part == null) {
            throw new ResourceNotFoundException("Repuesto no encontrado");
        }
        if (!part.isActive()) {
            throw new ResourceConflictException("El repuesto no está activo");
        }
        if (part.getStock() < dto.quantity()) {
            throw new ResourceConflictException("Stock insuficiente. Stock disponible: " + part.getStock() + ", solicitado: " + dto.quantity());
        }
        if (dto.quantity() <= 0) {
            throw new ResourceConflictException("La cantidad debe ser mayor a cero");
        }
        
        // Crear el consumo de repuesto
        ItemPart itemPart = new ItemPart();
        itemPart.setOrderItemId(itemId);
        itemPart.setPartId(dto.partId());
        itemPart.setQuantity(dto.quantity());
        itemPart.setUnitPrice(dto.unitPrice());
        itemPartMapper.insert(itemPart);
        
        // Actualizar stock del repuesto (disminuir)
        int delta = -dto.quantity();
        partMapper.updateStock(dto.partId(), delta);
        
        // Crear movimiento de inventario (salida)
        Movement movement = new Movement();
        movement.setId(UUID.randomUUID());
        movement.setPartId(dto.partId());
        movement.setType(MovementType.OUT);
        movement.setQuantity(dto.quantity());
        movement.setPerformedAt(Instant.now());
        movement.setNotes("Consumo en orden " + orderId + ", item " + itemId);
        movementMapper.insert(movement);
        
        recalculateTotal(orderId);
        return toItemPartDto(itemPart);
    }

    @Override
    public ItemPartDto patchItemPart(UUID orderId, UUID itemId, UUID repuestoId, ItemPartPatchDto dto) {
        // Validar que la orden no esté completada
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ResourceConflictException("No se pueden modificar repuestos de una orden completada");
        }
        
        ItemPart itemPart = itemPartMapper.find(itemId, repuestoId);
        if (itemPart == null) {
            throw new ResourceNotFoundException("Consumo no encontrado");
        }
        
        // Si se cambia la cantidad, validar stock y actualizar
        if (dto.quantity() != null && !dto.quantity().equals(itemPart.getQuantity())) {
            if (dto.quantity() <= 0) {
                throw new ResourceConflictException("La cantidad debe ser mayor a cero");
            }
            Part part = partMapper.findById(repuestoId);
            if (part == null) {
                throw new ResourceNotFoundException("Repuesto no encontrado");
            }
            
            int diferencia = dto.quantity() - itemPart.getQuantity();
            int stockDisponible = part.getStock();
            
            // Si se aumenta la cantidad, validar que hay stock suficiente
            if (diferencia > 0 && stockDisponible < diferencia) {
                throw new ResourceConflictException("Stock insuficiente. Stock disponible: " + stockDisponible + ", cantidad adicional solicitada: " + diferencia);
            }
            
            // Actualizar stock
            partMapper.updateStock(repuestoId, -diferencia);
            
            // Crear movimiento de inventario si hay cambio
            if (diferencia != 0) {
                Movement movement = new Movement();
                movement.setId(UUID.randomUUID());
                movement.setPartId(repuestoId);
                movement.setType(diferencia > 0 ? MovementType.OUT : MovementType.IN);
                movement.setQuantity(Math.abs(diferencia));
                movement.setPerformedAt(Instant.now());
                movement.setNotes("Ajuste en orden " + orderId + ", item " + itemId + ". Cantidad anterior: " + itemPart.getQuantity() + ", nueva: " + dto.quantity());
                movementMapper.insert(movement);
            }
            
            itemPart.setQuantity(dto.quantity());
        }
        if (dto.unitPrice() != null) itemPart.setUnitPrice(dto.unitPrice());
        itemPartMapper.update(itemPart);
        recalculateTotal(orderId);
        return toItemPartDto(itemPart);
    }

    @Override
    public void removeItemPart(UUID orderId, UUID itemId, Long repuestoId) {
        // Validar que la orden no esté completada
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ResourceConflictException("No se pueden modificar repuestos de una orden completada");
        }
        
        UUID partId = normalizePartId(repuestoId);
        ItemPart itemPart = itemPartMapper.find(itemId, partId);
        if (itemPart == null) {
            throw new ResourceNotFoundException("Consumo no encontrado");
        }
        
        // Restaurar stock del repuesto (aumentar)
        int cantidadARestaurar = itemPart.getQuantity();
        partMapper.updateStock(partId, cantidadARestaurar);
        
        // Crear movimiento de inventario (entrada - devolución)
        Movement movement = new Movement();
        movement.setId(UUID.randomUUID());
        movement.setPartId(partId);
        movement.setType(MovementType.IN);
        movement.setQuantity(cantidadARestaurar);
        movement.setPerformedAt(Instant.now());
        movement.setNotes("Devolución de consumo en orden " + orderId + ", item " + itemId);
        movementMapper.insert(movement);
        
        itemPartMapper.delete(itemId, partId);
        recalculateTotal(orderId);
    }

    private UUID normalizePartId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("El identificador del repuesto es obligatorio");
        }
        return new UUID(0L, value);
    }

    private void recalculateTotal(UUID orderId) {
        List<OrderItem> items = orderItemMapper.findByOrder(orderId, Integer.MAX_VALUE, 0);
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            List<ItemPart> parts = itemPartMapper.findByOrderItem(item.getId(), Integer.MAX_VALUE, 0);
            for (ItemPart part : parts) {
                total = total.add(part.getUnitPrice().multiply(BigDecimal.valueOf(part.getQuantity())));
            }
        }
        Order order = orderMapper.findById(orderId);
        if (order != null) {
            order.setTotal(total);
            order.setUpdatedAt(Instant.now());
            orderMapper.update(order);
        }
    }

    private OrderDto toDto(Order order) {
        return new OrderDto(order.getId(), order.getClientId(), order.getLicensePlate(), order.getStatus(), order.getDescription(), order.getTotal(), order.getCreatedAt(), order.getUpdatedAt());
    }

    private OrderItemDto toItemDto(OrderItem item) {
        return new OrderItemDto(item.getId(), item.getOrderId(), item.getServiceId(), item.getDescription(), item.getQuantity(), item.getUnitPrice());
    }

    private AssignmentDto toAssignmentDto(Assignment assignment) {
        return new AssignmentDto(assignment.getOrderItemId(), assignment.getMechanicId(), assignment.getAssignedAt(), assignment.getEstimatedHours());
    }

    private ItemPartDto toItemPartDto(ItemPart part) {
        return new ItemPartDto(part.getOrderItemId(), part.getPartId(), part.getQuantity(), part.getUnitPrice());
    }
}
