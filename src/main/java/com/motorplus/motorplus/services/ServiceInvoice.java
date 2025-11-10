package com.motorplus.motorplus.services;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServiceInvoice {
    InvoiceDto generateFromOrder(UUID orderId); // crea factura + líneas
    Page<InvoiceDto> list(InvoiceFilter f, Pageable p);
    InvoiceDto get(UUID id);
    InvoiceDto patch(UUID id, InvoicePatchDto dto);
    void delete(UUID id);

    // Líneas
    Page<InvoiceLineDto> listLines(UUID invoiceId, Pageable p);
    InvoiceLineDto addLine(UUID invoiceId, InvoiceLineCreateDto dto);
    InvoiceLineDto patchLine(UUID invoiceId, LineType tipo, UUID refId, InvoiceLinePatchDto dto);
    void removeLine(UUID invoiceId, LineType tipo, UUID refId);

    // Pagos
    Page<PaymentDto> listPayments(UUID invoiceId, Pageable p);
    PaymentDto addPayment(UUID invoiceId, PaymentCreateDto dto); // marca pagada si corresponde
    void removePayment(UUID invoiceId, UUID pagoId);
}
