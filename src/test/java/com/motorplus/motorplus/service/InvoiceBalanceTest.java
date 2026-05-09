package com.motorplus.motorplus.service;

import com.motorplus.motorplus.dto.invoiceDtos.InvoiceStatus;
import com.motorplus.motorplus.dto.invoiceDtos.LineType;
import com.motorplus.motorplus.dto.invoiceDtos.PaymentCreateDto;
import com.motorplus.motorplus.mapper.InvoiceLineMapper;
import com.motorplus.motorplus.mapper.InvoiceMapper;
import com.motorplus.motorplus.mapper.ItemPartMapper;
import com.motorplus.motorplus.mapper.OrderItemMapper;
import com.motorplus.motorplus.mapper.OrderMapper;
import com.motorplus.motorplus.mapper.PaymentMapper;
import com.motorplus.motorplus.model.Invoice;
import com.motorplus.motorplus.model.InvoiceLine;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceBalanceTest {

    @Mock private InvoiceMapper invoiceMapper;
    @Mock private InvoiceLineMapper invoiceLineMapper;
    @Mock private PaymentMapper paymentMapper;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private ItemPartMapper itemPartMapper;

    private ServiceInvoiceImpl service;

    private static final BigDecimal TOTAL = new BigDecimal("119.00");
    private UUID invoiceId;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        service = new ServiceInvoiceImpl(
                invoiceMapper, invoiceLineMapper, paymentMapper,
                orderMapper, orderItemMapper, itemPartMapper);

        invoiceId = UUID.randomUUID();

        invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setOrderId(UUID.randomUUID());
        invoice.setNumber("INV-TEST-001");
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssueDate(Instant.now());
        invoice.setDueDate(Instant.now().plusSeconds(604800));
        invoice.setTotal(TOTAL);
        invoice.setBalance(TOTAL);
    }

    @Test
    void addPayment_setsStatusPaid_whenPaymentCoversFullBalance() {
        // addPayment busca la factura y luego adjustTotals la vuelve a buscar
        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        // Líneas: servicio (100) + IVA (19) — hasTaxLine=true, por eso adjustTotals suma todo
        InvoiceLine serviceLineObj = line("Mano de obra", new BigDecimal("100.00"));
        InvoiceLine taxLineObj     = line("IVA (19%)",    new BigDecimal("19.00"));
        when(invoiceLineMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(serviceLineObj, taxLineObj));

        // El pago cubre el total completo
        Payment fullPayment = new Payment();
        fullPayment.setId(UUID.randomUUID());
        fullPayment.setInvoiceId(invoiceId);
        fullPayment.setAmount(TOTAL);
        when(paymentMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(fullPayment));

        service.addPayment(invoiceId, new PaymentCreateDto(TOTAL, "CASH", "REF-001"));

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(captor.capture());
        Invoice saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void addPayment_keepsStatusIssued_whenPaymentIsPartial() {
        when(invoiceMapper.findById(invoiceId)).thenReturn(invoice);

        InvoiceLine serviceLineObj = line("Mano de obra", new BigDecimal("100.00"));
        InvoiceLine taxLineObj     = line("IVA (19%)",    new BigDecimal("19.00"));
        when(invoiceLineMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(serviceLineObj, taxLineObj));

        // Pago parcial: solo 50 de 119
        BigDecimal partial = new BigDecimal("50.00");
        Payment partialPayment = new Payment();
        partialPayment.setId(UUID.randomUUID());
        partialPayment.setInvoiceId(invoiceId);
        partialPayment.setAmount(partial);
        when(paymentMapper.findByInvoice(eq(invoiceId), anyInt(), anyLong()))
                .thenReturn(List.of(partialPayment));

        service.addPayment(invoiceId, new PaymentCreateDto(partial, "CARD", "REF-002"));

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(captor.capture());
        Invoice saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(saved.getBalance()).isEqualByComparingTo(new BigDecimal("69.00"));
    }

    private InvoiceLine line(String description, BigDecimal amount) {
        InvoiceLine l = new InvoiceLine();
        l.setInvoiceId(invoiceId);
        l.setType(LineType.MANUAL);
        l.setReferenceId(UUID.randomUUID());
        l.setDescription(description);
        l.setAmount(amount);
        return l;
    }
}
