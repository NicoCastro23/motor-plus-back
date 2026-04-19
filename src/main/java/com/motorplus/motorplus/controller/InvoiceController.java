package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.invoiceDtos.InvoiceDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceFilter;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceLineCreateDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceLineDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceLinePatchDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoicePatchDto;
import com.motorplus.motorplus.dto.invoiceDtos.LineType;
import com.motorplus.motorplus.dto.invoiceDtos.PaymentCreateDto;
import com.motorplus.motorplus.dto.invoiceDtos.PaymentDto;
import com.motorplus.motorplus.services.ServiceInvoice;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class InvoiceController {

    private final ServiceInvoice serviceInvoice;

    public InvoiceController(ServiceInvoice serviceInvoice) {
        this.serviceInvoice = serviceInvoice;
    }

    @PostMapping("/from-order/{orderId}")
    public ResponseEntity<InvoiceDto> generateFromOrder(@PathVariable("orderId") UUID orderId) {
        InvoiceDto invoice = serviceInvoice.generateFromOrder(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping
    public Page<InvoiceDto> list(@ModelAttribute InvoiceFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return serviceInvoice.list(filter, pageable);
    }

    @GetMapping("/{id}")
    public InvoiceDto get(@PathVariable("id") UUID id) {
        return serviceInvoice.get(id);
    }

    @PatchMapping("/{id}")
    public InvoiceDto patch(@PathVariable("id") UUID id, @RequestBody InvoicePatchDto dto) {
        return serviceInvoice.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        serviceInvoice.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/lines")
    public Page<InvoiceLineDto> listLines(@PathVariable("id") UUID id, @PageableDefault(size = 20) Pageable pageable) {
        return serviceInvoice.listLines(id, pageable);
    }

    @PostMapping("/{id}/lines")
    public ResponseEntity<InvoiceLineDto> addLine(@PathVariable("id") UUID id, @Valid @RequestBody InvoiceLineCreateDto dto) {
        InvoiceLineDto line = serviceInvoice.addLine(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(line);
    }

    @PatchMapping("/{id}/lines/{type}/{refId}")
    public InvoiceLineDto patchLine(@PathVariable("id") UUID id,
                                    @PathVariable("type") LineType type,
                                    @PathVariable("refId") UUID refId,
                                    @RequestBody InvoiceLinePatchDto dto) {
        return serviceInvoice.patchLine(id, type, refId, dto);
    }

    @DeleteMapping("/{id}/lines/{type}/{refId}")
    public ResponseEntity<Void> removeLine(@PathVariable("id") UUID id,
                                           @PathVariable("type") LineType type,
                                           @PathVariable("refId") UUID refId) {
        serviceInvoice.removeLine(id, type, refId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/payments")
    public Page<PaymentDto> listPayments(@PathVariable("id") UUID id, @PageableDefault(size = 20) Pageable pageable) {
        return serviceInvoice.listPayments(id, pageable);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<PaymentDto> addPayment(@PathVariable("id") UUID id, @Valid @RequestBody PaymentCreateDto dto) {
        PaymentDto payment = serviceInvoice.addPayment(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @DeleteMapping("/{invoiceId}/payments/{paymentId}")
    public ResponseEntity<Void> removePayment(@PathVariable("invoiceId") UUID invoiceId, @PathVariable("paymentId") UUID paymentId) {
        serviceInvoice.removePayment(invoiceId, paymentId);
        return ResponseEntity.noContent().build();
    }
}
