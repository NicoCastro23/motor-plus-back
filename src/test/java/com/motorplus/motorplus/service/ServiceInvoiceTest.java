package com.motorplus.motorplus.service;

import com.motorplus.motorplus.dto.invoiceDtos.InvoiceDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceLinePatchDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceStatus;
import com.motorplus.motorplus.dto.invoiceDtos.LineType;
import com.motorplus.motorplus.dto.invoiceDtos.PaymentCreateDto;
import com.motorplus.motorplus.dto.ordersDtos.OrderStatus;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.InvoiceLineMapper;
import com.motorplus.motorplus.mapper.InvoiceMapper;
import com.motorplus.motorplus.mapper.ItemPartMapper;
import com.motorplus.motorplus.mapper.OrderItemMapper;
import com.motorplus.motorplus.mapper.OrderMapper;
import com.motorplus.motorplus.mapper.PaymentMapper;
import com.motorplus.motorplus.model.Invoice;
import com.motorplus.motorplus.model.InvoiceLine;
import com.motorplus.motorplus.model.ItemPart;
import com.motorplus.motorplus.model.Order;
import com.motorplus.motorplus.model.OrderItem;
import com.motorplus.motorplus.model.Payment;
import com.motorplus.motorplus.services.impl.ServiceInvoiceImpl;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceInvoiceTest {

    @Mock private InvoiceMapper invoiceMapper;
    @Mock private InvoiceLineMapper invoiceLineMapper;
    @Mock private PaymentMapper paymentMapper;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private ItemPartMapper itemPartMapper;

    private ServiceInvoiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServiceInvoiceImpl(
                invoiceMapper, invoiceLineMapper, paymentMapper,
                orderMapper, orderItemMapper, itemPartMapper);
    }

    // ──────────────────────────────────────────────────────────
    // generateFromOrder()
    // ──────────────────────────────────────────────────────────

    @Test
    void generateFromOrder_throwsNotFound_whenOrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        when(orderMapper.findById(orderId)).thenReturn(null);

        assertThatThrownBy(() -> service.generateFromOrder(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Orden no encontrada");

        verify(invoiceMapper, never()).insert(any());
    }

    @Test
    void generateFromOrder_throwsConflict_whenOrderIsNotCompleted() {
        UUID orderId = UUID.randomUUID();
        Order order = orderWith(orderId, OrderStatus.IN_PROGRESS);
        when(orderMapper.findById(orderId)).thenReturn(order);

        assertThatThrownBy(() -> service.generateFromOrder(orderId))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("completadas");

        verify(invoiceMapper, never()).insert(any());
    }

    @Test
    void generateFromOrder_throwsConflict_whenOrderHasNoItems() {
        UUID orderId = UUID.randomUUID();
        Order order = orderWith(orderId, OrderStatus.COMPLETED);
        when(orderMapper.findById(orderId)).thenReturn(order);
        when(invoiceMapper.findAll(eq(orderId), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.generateFromOrder(orderId))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("sin items");

        verify(invoiceMapper, never()).insert(any());
    }

    @Test
    void generateFromOrder_createsInvoiceWithServiceLinesAndIva() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.COMPLETED);
        when(orderMapper.findById(orderId)).thenReturn(order);
        when(invoiceMapper.findAll(eq(orderId), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setDescription("Cambio de aceite");
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(BigDecimal.valueOf(100));
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));

        // No parts for this order item
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());

        // No duplicate service line exists
        when(invoiceLineMapper.find(any(), eq(LineType.SERVICE), eq(itemId))).thenReturn(null);
        // No duplicate tax line exists
        when(invoiceLineMapper.find(any(), eq(LineType.MANUAL), any())).thenReturn(null);

        InvoiceDto result = service.generateFromOrder(orderId);

        // Invoice was inserted
        verify(invoiceMapper).insert(any(Invoice.class));

        // Service line and tax line were inserted (2 inserts total)
        verify(invoiceLineMapper, times(2)).insert(any(InvoiceLine.class));

        // Totals were updated: subtotal=100, IVA=19, total=119
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getTotal()).isEqualByComparingTo("119.00");
        assertThat(updated.getBalance()).isEqualByComparingTo("119.00");
        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test
    void generateFromOrder_deletesExistingInvoicesBeforeCreatingNew() {
        UUID orderId = UUID.randomUUID();
        UUID existingInvoiceId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.COMPLETED);
        when(orderMapper.findById(orderId)).thenReturn(order);

        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(existingInvoiceId);
        when(invoiceMapper.findAll(eq(orderId), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(List.of(existingInvoice));

        UUID itemId = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setDescription("Servicio");
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(BigDecimal.valueOf(50));
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));

        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(invoiceLineMapper.find(any(), any(), any())).thenReturn(null);

        service.generateFromOrder(orderId);

        verify(invoiceMapper).delete(existingInvoiceId);
    }

    @Test
    void generateFromOrder_includesPartLinesWithAggregatedAmounts() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();

        Order order = orderWith(orderId, OrderStatus.COMPLETED);
        when(orderMapper.findById(orderId)).thenReturn(order);
        when(invoiceMapper.findAll(eq(orderId), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(Collections.emptyList());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setDescription("Servicio");
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(BigDecimal.valueOf(200));
        when(orderItemMapper.findByOrder(eq(orderId), anyInt(), anyLong()))
                .thenReturn(List.of(orderItem));

        ItemPart itemPart = new ItemPart();
        itemPart.setPartId(partId);
        itemPart.setQuantity(2);
        itemPart.setUnitPrice(BigDecimal.valueOf(50)); // 2 * 50 = 100
        when(itemPartMapper.findByOrderItem(eq(itemId), anyInt(), anyLong()))
                .thenReturn(List.of(itemPart));

        when(invoiceLineMapper.find(any(), any(), any())).thenReturn(null);

        service.generateFromOrder(orderId);

        // Lines: 1 service + 1 part + 1 IVA = 3 inserts
        verify(invoiceLineMapper, times(3)).insert(any(InvoiceLine.class));

        // subtotal = 200 + 100 = 300; IVA = 57; total = 357
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(captor.capture());
        assertThat(captor.getValue().getTotal()).isEqualByComparingTo("357.00");
    }

    // ──────────────────────────────────────────────────────────
    // get()
    // ──────────────────────────────────────────────────────────

    @Test
    void get_throwsNotFound_whenInvoiceDoesNotExist() {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceMapper.findById(invoiceId)).thenReturn(null);

        assertThatThrownBy(() -> service.get(invoiceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Factura no encontrada");
    }

    @Test
    void get_returnsDto_whenInvoiceExists() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = invoiceWith(invoiceId, InvoiceStatus.ISSUED, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        InvoiceDto dto = service.get(invoiceId);

        assertThat(dto.id()).isEqualTo(invoiceId);
        assertThat(dto.status()).isEqualTo(InvoiceStatus.ISSUED);
    }

    // ──────────────────────────────────────────────────────────
    // delete()
    // ──────────────────────────────────────────────────────────

    @Test
    void delete_throwsNotFound_whenInvoiceDoesNotExist() {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceMapper.findById(invoiceId)).thenReturn(null);

        assertThatThrownBy(() -> service.delete(invoiceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Factura no encontrada");

        verify(invoiceMapper, never()).delete(any());
    }

    @Test
    void delete_succeeds_whenInvoiceExists() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = invoiceWith(invoiceId, InvoiceStatus.ISSUED, BigDecimal.TEN, BigDecimal.TEN);
        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        service.delete(invoiceId);

        verify(invoiceMapper).delete(invoiceId);
    }

    // ──────────────────────────────────────────────────────────
    // addPayment() — marks PAID when balance reaches zero
    // ──────────────────────────────────────────────────────────

    @Test
    void addPayment_throwsNotFound_whenInvoiceDoesNotExist() {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceMapper.findById(invoiceId)).thenReturn(null);

        assertThatThrownBy(() -> service.addPayment(invoiceId, new PaymentCreateDto(BigDecimal.TEN, "CASH", "REF-01")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Factura no encontrada");
    }

    @Test
    void addPayment_marksInvoiceAsPaid_whenPaymentCoversFullBalance() {
        UUID invoiceId = UUID.randomUUID();
        BigDecimal total = BigDecimal.valueOf(119);
        Invoice invoice = invoiceWith(invoiceId, InvoiceStatus.ISSUED, total, total);
        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        // Payment for full amount
        when(paymentMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(paymentOf(invoiceId, total)));

        // adjustTotals re-fetches invoice and lines
        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        InvoiceLine taxLine = new InvoiceLine();
        taxLine.setDescription("IVA (19%)");
        taxLine.setAmount(BigDecimal.valueOf(19));
        InvoiceLine serviceLine = new InvoiceLine();
        serviceLine.setDescription("Servicio");
        serviceLine.setAmount(BigDecimal.valueOf(100));

        when(invoiceLineMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(serviceLine, taxLine));

        service.addPayment(invoiceId, new PaymentCreateDto(total, "CASH", "REF-99"));

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void addPayment_doesNotMarkPaid_whenPartialPayment() {
        UUID invoiceId = UUID.randomUUID();
        BigDecimal total = BigDecimal.valueOf(200);
        BigDecimal partial = BigDecimal.valueOf(50);
        Invoice invoice = invoiceWith(invoiceId, InvoiceStatus.ISSUED, total, total);
        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        when(paymentMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(paymentOf(invoiceId, partial)));

        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        InvoiceLine taxLine = new InvoiceLine();
        taxLine.setDescription("IVA (19%)");
        taxLine.setAmount(BigDecimal.valueOf(30));
        InvoiceLine serviceLine = new InvoiceLine();
        serviceLine.setDescription("Servicio");
        serviceLine.setAmount(BigDecimal.valueOf(170));

        when(invoiceLineMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(serviceLine, taxLine));

        service.addPayment(invoiceId, new PaymentCreateDto(partial, "CARD", "REF-50"));

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isNotEqualTo(InvoiceStatus.PAID);
    }

    // ──────────────────────────────────────────────────────────
    // patchLine()
    // ──────────────────────────────────────────────────────────

    @Test
    void patchLine_throwsNotFound_whenLineDoesNotExist() {
        UUID invoiceId = UUID.randomUUID();
        UUID refId = UUID.randomUUID();
        when(invoiceLineMapper.find(invoiceId, LineType.SERVICE, refId)).thenReturn(null);

        assertThatThrownBy(() -> service.patchLine(invoiceId, LineType.SERVICE, refId,
                new InvoiceLinePatchDto("updated desc", BigDecimal.TEN)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Línea no encontrada");
    }

    // ──────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────

    private Order orderWith(UUID id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setTotal(BigDecimal.ZERO);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        return order;
    }

    private Invoice invoiceWith(UUID id, InvoiceStatus status, BigDecimal total, BigDecimal balance) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setOrderId(UUID.randomUUID());
        invoice.setNumber("INV-TEST");
        invoice.setStatus(status);
        invoice.setIssueDate(Instant.now());
        invoice.setDueDate(Instant.now().plusSeconds(604800));
        invoice.setTotal(total);
        invoice.setBalance(balance);
        return invoice;
    }

    private Payment paymentOf(UUID invoiceId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setMethod("CASH");
        payment.setPaymentDate(Instant.now());
        return payment;
    }
}
