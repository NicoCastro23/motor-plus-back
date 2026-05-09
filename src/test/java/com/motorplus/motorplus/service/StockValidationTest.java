package com.motorplus.motorplus.service;

import com.motorplus.motorplus.dto.ordersDtos.OrderStatus;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockValidationTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private AssignmentMapper assignmentMapper;
    @Mock private ItemPartMapper itemPartMapper;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private PartMapper partMapper;
    @Mock private MovementMapper movementMapper;
    @Mock private ServiceInvoice serviceInvoice;

    private OrderServiceImpl service;

    private UUID orderId;
    private UUID itemId;
    private UUID partId;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                orderMapper, orderItemMapper, assignmentMapper,
                itemPartMapper, vehicleMapper, partMapper,
                movementMapper, serviceInvoice);

        orderId = UUID.randomUUID();
        itemId  = UUID.randomUUID();
        partId  = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setTotal(BigDecimal.ZERO);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        when(orderMapper.findById(orderId)).thenReturn(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        // findByOrder se invoca en validateStockForOrder y también en deductStockForOrder
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));
    }

    @Test
    void changeStatus_throwsConflict_whenStockIsBelowRequired() {
        ItemPart itemPart = itemPartWith(partId, /*quantity*/ 10);
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        Part part = partWith(partId, "Filtro de aceite", /*stock*/ 3, true);
        when(partMapper.findById(partId)).thenReturn(part);

        assertThatThrownBy(() -> service.changeStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Filtro de aceite")
                .hasMessageContaining("disponible: 3")
                .hasMessageContaining("requerido: 10");

        // El descuento de stock nunca debe ejecutarse si la validación falla
        verify(partMapper, never()).updateStock(eq(partId), anyInt());
    }

    @Test
    void changeStatus_throwsConflict_whenPartNotFound() {
        ItemPart itemPart = itemPartWith(partId, 1);
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        when(partMapper.findById(partId)).thenReturn(null);

        assertThatThrownBy(() -> service.changeStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("no existe o está inactivo");
    }

    @Test
    void changeStatus_throwsConflict_whenPartIsInactive() {
        ItemPart itemPart = itemPartWith(partId, 1);
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        Part part = partWith(partId, "Bujía", /*stock*/ 50, /*active*/ false);
        when(partMapper.findById(partId)).thenReturn(part);

        assertThatThrownBy(() -> service.changeStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("no existe o está inactivo");
    }

    @Test
    void changeStatus_throwsConflict_onSecondPartWhenFirstIsOk() {
        UUID partId2 = UUID.randomUUID();

        ItemPart first  = itemPartWith(partId,  2);
        ItemPart second = itemPartWith(partId2, 5);
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(first, second));

        // Primer repuesto: stock suficiente
        Part okPart = partWith(partId, "Aceite motor", /*stock*/ 10, true);
        when(partMapper.findById(partId)).thenReturn(okPart);

        // Segundo repuesto: stock insuficiente
        Part lowPart = partWith(partId2, "Correa de distribución", /*stock*/ 1, true);
        when(partMapper.findById(partId2)).thenReturn(lowPart);

        assertThatThrownBy(() -> service.changeStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Correa de distribución")
                .hasMessageContaining("disponible: 1")
                .hasMessageContaining("requerido: 5");
    }

    @Test
    void changeStatus_throwsConflict_whenStockIsExactlyOneBelowRequired() {
        ItemPart itemPart = itemPartWith(partId, 5);
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        // stock = required - 1 → debe fallar
        Part part = partWith(partId, "Pastillas de freno", /*stock*/ 4, true);
        when(partMapper.findById(partId)).thenReturn(part);

        assertThatThrownBy(() -> service.changeStatus(orderId, OrderStatus.COMPLETED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("disponible: 4")
                .hasMessageContaining("requerido: 5");
    }

    // ──────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────

    private ItemPart itemPartWith(UUID pId, int quantity) {
        ItemPart ip = new ItemPart();
        ip.setOrderItemId(itemId);
        ip.setPartId(pId);
        ip.setQuantity(quantity);
        ip.setUnitPrice(BigDecimal.TEN);
        return ip;
    }

    private Part partWith(UUID pId, String name, int stock, boolean active) {
        Part p = new Part();
        p.setId(pId);
        p.setName(name);
        p.setStock(stock);
        p.setActive(active);
        return p;
    }
}
