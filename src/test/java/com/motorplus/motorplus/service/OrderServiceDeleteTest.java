package com.motorplus.motorplus.service;

import com.motorplus.motorplus.dto.itemPartDtos.ItemPartCreateDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderStatus;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.AssignmentMapper;
import com.motorplus.motorplus.mapper.ItemPartMapper;
import com.motorplus.motorplus.mapper.MovementMapper;
import com.motorplus.motorplus.mapper.OrderItemMapper;
import com.motorplus.motorplus.mapper.OrderMapper;
import com.motorplus.motorplus.mapper.PartMapper;
import com.motorplus.motorplus.mapper.VehicleMapper;
import com.motorplus.motorplus.model.ItemPart;
import com.motorplus.motorplus.model.Order;
import com.motorplus.motorplus.model.OrderItem;
import com.motorplus.motorplus.model.Part;
import com.motorplus.motorplus.services.ServiceInvoice;
import com.motorplus.motorplus.services.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceDeleteTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private AssignmentMapper assignmentMapper;
    @Mock private ItemPartMapper itemPartMapper;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private PartMapper partMapper;
    @Mock private MovementMapper movementMapper;
    @Mock private ServiceInvoice serviceInvoice;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                orderMapper, orderItemMapper, assignmentMapper,
                itemPartMapper, vehicleMapper, partMapper,
                movementMapper, serviceInvoice);
    }

    // ──────────────────────────────────────────────────────────
    // delete()
    // ──────────────────────────────────────────────────────────

    @Test
    void delete_throwsNotFound_whenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(orderMapper.findById(id)).thenReturn(null);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Orden no encontrada");

        verify(orderMapper, never()).delete(any());
    }

    @Test
    void delete_throwsConflict_whenOrderIsCompleted() {
        UUID id = UUID.randomUUID();
        Order order = orderWith(id, OrderStatus.COMPLETED);
        when(orderMapper.findById(id)).thenReturn(order);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("No se puede eliminar una orden completada");

        verify(orderMapper, never()).delete(any());
    }

    @Test
    void delete_succeeds_whenOrderIsInDraft() {
        UUID id = UUID.randomUUID();
        Order order = orderWith(id, OrderStatus.DRAFT);
        when(orderMapper.findById(id)).thenReturn(order);

        service.delete(id);

        verify(orderMapper).delete(id);
    }

    @Test
    void delete_succeeds_whenOrderIsInProgress() {
        UUID id = UUID.randomUUID();
        Order order = orderWith(id, OrderStatus.IN_PROGRESS);
        when(orderMapper.findById(id)).thenReturn(order);

        service.delete(id);

        verify(orderMapper).delete(id);
    }

    // ──────────────────────────────────────────────────────────
    // changeStatus()
    // ──────────────────────────────────────────────────────────

    @Test
    void changeStatus_throwsNotFound_whenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(orderMapper.findById(id)).thenReturn(null);

        assertThatThrownBy(() -> service.changeStatus(id, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changeStatus_throwsConflict_whenInsufficientStock() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.IN_PROGRESS);
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));

        ItemPart itemPart = new ItemPart();
        itemPart.setPartId(partId);
        itemPart.setQuantity(10);
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        Part part = new Part();
        part.setId(partId);
        part.setName("Filtro de aceite");
        part.setActive(true);
        part.setStock(3); // menos que la cantidad requerida
        when(partMapper.findById(partId)).thenReturn(part);

        assertThatThrownBy(() -> service.changeStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void changeStatus_throwsConflict_whenPartIsInactive() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.IN_PROGRESS);
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));

        ItemPart itemPart = new ItemPart();
        itemPart.setPartId(partId);
        itemPart.setQuantity(1);
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        Part part = new Part();
        part.setId(partId);
        part.setActive(false);
        when(partMapper.findById(partId)).thenReturn(part);

        assertThatThrownBy(() -> service.changeStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void changeStatus_toCompleted_deductsStockAndGeneratesInvoice() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.IN_PROGRESS);
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setUnitPrice(BigDecimal.TEN);
        orderItem.setQuantity(1);

        // findByOrder se llama en validateStock, deductStock y en el bloque de factura
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));

        ItemPart itemPart = new ItemPart();
        itemPart.setPartId(partId);
        itemPart.setQuantity(2);
        itemPart.setUnitPrice(BigDecimal.valueOf(50));
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        Part part = new Part();
        part.setId(partId);
        part.setName("Bujía");
        part.setActive(true);
        part.setStock(10);
        when(partMapper.findById(partId)).thenReturn(part);

        service.changeStatus(orderId, OrderStatus.COMPLETED);

        verify(partMapper).updateStock(partId, -2);
        verify(movementMapper).insert(any());
        verify(serviceInvoice).generateFromOrder(orderId);
    }

    @Test
    void changeStatus_toCancelled_doesNotTouchStockOrInvoice() {
        UUID orderId = UUID.randomUUID();
        Order order = orderWith(orderId, OrderStatus.IN_PROGRESS);
        when(orderMapper.findById(orderId)).thenReturn(order);

        service.changeStatus(orderId, OrderStatus.CANCELLED);

        verify(partMapper, never()).updateStock(any(), anyInt());
        verify(serviceInvoice, never()).generateFromOrder(any());
    }

    // ──────────────────────────────────────────────────────────
    // removeItem()
    // ──────────────────────────────────────────────────────────

    @Test
    void removeItem_throwsConflict_whenOrderIsCompleted() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.COMPLETED);
        when(orderMapper.findById(orderId)).thenReturn(order);

        assertThatThrownBy(() -> service.removeItem(orderId, itemId))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("completada");

        verify(orderItemMapper, never()).delete(any(), any());
    }

    @Test
    void removeItem_throwsNotFound_whenItemDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.DRAFT);
        when(orderMapper.findById(orderId)).thenReturn(order);
        when(orderItemMapper.findById(orderId, itemId)).thenReturn(null);

        assertThatThrownBy(() -> service.removeItem(orderId, itemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item no encontrado");
    }

    // ──────────────────────────────────────────────────────────
    // addItemPart()
    // ──────────────────────────────────────────────────────────

    @Test
    void addItemPart_throwsConflict_whenOrderIsCompleted() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        ItemPartCreateDto dto = new ItemPartCreateDto(partId, 1, BigDecimal.TEN);

        Order order = orderWith(orderId, OrderStatus.COMPLETED);
        when(orderMapper.findById(orderId)).thenReturn(order);

        assertThatThrownBy(() -> service.addItemPart(orderId, itemId, dto))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("completada");
    }

    @Test
    void addItemPart_throwsNotFound_whenPartDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        ItemPartCreateDto dto = new ItemPartCreateDto(partId, 1, BigDecimal.TEN);

        Order order = orderWith(orderId, OrderStatus.DRAFT);
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem item = new OrderItem();
        item.setId(itemId);
        when(orderItemMapper.findById(orderId, itemId)).thenReturn(item);
        when(partMapper.findById(partId)).thenReturn(null);

        assertThatThrownBy(() -> service.addItemPart(orderId, itemId, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Repuesto no encontrado");
    }

    @Test
    void addItemPart_throwsConflict_whenPartIsInactive() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        ItemPartCreateDto dto = new ItemPartCreateDto(partId, 1, BigDecimal.TEN);

        Order order = orderWith(orderId, OrderStatus.DRAFT);
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem item = new OrderItem();
        item.setId(itemId);
        when(orderItemMapper.findById(orderId, itemId)).thenReturn(item);

        Part part = new Part();
        part.setActive(false);
        part.setStock(100);
        when(partMapper.findById(partId)).thenReturn(part);

        assertThatThrownBy(() -> service.addItemPart(orderId, itemId, dto))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("activo");
    }

    @Test
    void addItemPart_throwsConflict_whenInsufficientStock() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        ItemPartCreateDto dto = new ItemPartCreateDto(partId, 5, BigDecimal.TEN);

        Order order = orderWith(orderId, OrderStatus.DRAFT);
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem item = new OrderItem();
        item.setId(itemId);
        when(orderItemMapper.findById(orderId, itemId)).thenReturn(item);

        Part part = new Part();
        part.setActive(true);
        part.setStock(2);
        when(partMapper.findById(partId)).thenReturn(part);

        assertThatThrownBy(() -> service.addItemPart(orderId, itemId, dto))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void addItemPart_insertsRecord_whenStockIsSufficient() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        ItemPartCreateDto dto = new ItemPartCreateDto(partId, 3, BigDecimal.valueOf(25));

        Order order = orderWith(orderId, OrderStatus.DRAFT);
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setUnitPrice(BigDecimal.TEN);
        orderItem.setQuantity(1);
        when(orderItemMapper.findById(orderId, itemId)).thenReturn(orderItem);

        Part part = new Part();
        part.setActive(true);
        part.setStock(10);
        when(partMapper.findById(partId)).thenReturn(part);

        // recalculateTotal llama findByOrder y findByOrderItem
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));

        ItemPart insertedPart = new ItemPart();
        insertedPart.setPartId(partId);
        insertedPart.setQuantity(3);
        insertedPart.setUnitPrice(BigDecimal.valueOf(25));
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(insertedPart));

        when(orderMapper.findById(orderId)).thenReturn(order);

        service.addItemPart(orderId, itemId, dto);

        ArgumentCaptor<ItemPart> captor = ArgumentCaptor.forClass(ItemPart.class);
        verify(itemPartMapper).insert(captor.capture());
        assertThat(captor.getValue().getPartId()).isEqualTo(partId);
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);
    }

    // ──────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────

    private Order orderWith(UUID id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setLicensePlate("ABC123");
        order.setTotal(BigDecimal.ZERO);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        return order;
    }
}
