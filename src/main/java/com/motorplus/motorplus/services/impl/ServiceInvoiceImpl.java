package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.invoiceDtos.InvoiceDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceFilter;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceLineCreateDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceLineDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceLinePatchDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoicePatchDto;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceStatus;
import com.motorplus.motorplus.dto.invoiceDtos.LineType;
import com.motorplus.motorplus.dto.invoiceDtos.PaymentCreateDto;
import com.motorplus.motorplus.dto.invoiceDtos.PaymentDto;
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
import com.motorplus.motorplus.services.ServiceInvoice;
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
public class ServiceInvoiceImpl implements ServiceInvoice {

    private final InvoiceMapper invoiceMapper;
    private final InvoiceLineMapper invoiceLineMapper;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ItemPartMapper itemPartMapper;

    public ServiceInvoiceImpl(InvoiceMapper invoiceMapper, InvoiceLineMapper invoiceLineMapper, PaymentMapper paymentMapper, OrderMapper orderMapper, OrderItemMapper orderItemMapper, ItemPartMapper itemPartMapper) {
        this.invoiceMapper = invoiceMapper;
        this.invoiceLineMapper = invoiceLineMapper;
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.itemPartMapper = itemPartMapper;
    }

    @Override
    public InvoiceDto generateFromOrder(UUID orderId) {
        // PASO 1: Validar orden
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ResourceConflictException("Solo se pueden generar facturas para órdenes completadas. Estado actual: " + order.getStatus());
        }
        
        // PASO 2: Eliminar cualquier factura parcial previa (si existe)
        List<Invoice> existingInvoices = invoiceMapper.findAll(orderId, null, null, null, Integer.MAX_VALUE, 0);
        for (Invoice existing : existingInvoices) {
            invoiceMapper.delete(existing.getId());
        }
        
        // PASO 3: Obtener items de la orden
        List<OrderItem> items = orderItemMapper.findByOrder(orderId, Integer.MAX_VALUE, 0);
        if (items.isEmpty()) {
            throw new ResourceConflictException("No se puede generar una factura para una orden sin items de servicio");
        }
        
        // PASO 4: Crear factura nueva
        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setOrderId(orderId);
        invoice.setNumber("INV-" + System.currentTimeMillis());
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssueDate(Instant.now());
        invoice.setDueDate(Instant.now().plusSeconds(7 * 24 * 3600));
        invoice.setTotal(BigDecimal.ZERO);
        invoice.setBalance(BigDecimal.ZERO);
        invoiceMapper.insert(invoice);

        // PASO 5: Agrupar repuestos ANTES de insertar
        // Usar Map para acumular: partId -> cantidad total y monto total
        java.util.Map<UUID, java.util.Map<String, Object>> partsMap = new java.util.HashMap<>();
        
        // Recopilar todos los repuestos de todos los items
        for (OrderItem item : items) {
            List<ItemPart> parts = itemPartMapper.findByOrderItem(item.getId(), Integer.MAX_VALUE, 0);
            for (ItemPart part : parts) {
                UUID partId = part.getPartId();
                BigDecimal partAmount = part.getUnitPrice().multiply(BigDecimal.valueOf(part.getQuantity()));
                
                if (partsMap.containsKey(partId)) {
                    // Ya existe, sumar cantidad y monto
                    java.util.Map<String, Object> existing = partsMap.get(partId);
                    BigDecimal existingAmount = (BigDecimal) existing.get("amount");
                    existing.put("amount", existingAmount.add(partAmount));
                } else {
                    // Nuevo repuesto
                    java.util.Map<String, Object> partData = new java.util.HashMap<>();
                    partData.put("amount", partAmount);
                    partsMap.put(partId, partData);
                }
            }
        }
        
        // PASO 6: Insertar líneas de servicios
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            BigDecimal serviceAmount = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(serviceAmount);
            
            InvoiceLine serviceLine = new InvoiceLine();
            serviceLine.setInvoiceId(invoice.getId());
            serviceLine.setType(LineType.SERVICE);
            serviceLine.setReferenceId(item.getId());
            String desc = item.getDescription();
            serviceLine.setDescription(desc != null && !desc.trim().isEmpty() ? desc : "Servicio de mano de obra");
            serviceLine.setAmount(serviceAmount);
            
            // Verificar antes de insertar
            InvoiceLine existing = invoiceLineMapper.find(invoice.getId(), LineType.SERVICE, item.getId());
            if (existing == null) {
                invoiceLineMapper.insert(serviceLine);
            }
        }
        
        // PASO 7: Insertar líneas de repuestos (una por partId único)
        for (java.util.Map.Entry<UUID, java.util.Map<String, Object>> entry : partsMap.entrySet()) {
            UUID partId = entry.getKey();
            BigDecimal partAmount = (BigDecimal) entry.getValue().get("amount");
            subtotal = subtotal.add(partAmount);
            
            // Verificar antes de insertar para evitar duplicados
            InvoiceLine existing = invoiceLineMapper.find(invoice.getId(), LineType.PART, partId);
            if (existing == null) {
                InvoiceLine partLine = new InvoiceLine();
                partLine.setInvoiceId(invoice.getId());
                partLine.setType(LineType.PART);
                partLine.setReferenceId(partId);
                partLine.setDescription("Consumo repuesto");
                partLine.setAmount(partAmount);
                invoiceLineMapper.insert(partLine);
            }
        }

        // PASO 8: Calcular e insertar IVA
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.19"));
        BigDecimal total = subtotal.add(taxAmount);
        
        UUID taxRefId = UUID.randomUUID();
        InvoiceLine existingTax = invoiceLineMapper.find(invoice.getId(), LineType.MANUAL, taxRefId);
        if (existingTax == null) {
            InvoiceLine taxLine = new InvoiceLine();
            taxLine.setInvoiceId(invoice.getId());
            taxLine.setType(LineType.MANUAL);
            taxLine.setReferenceId(taxRefId);
            taxLine.setDescription("IVA (19%)");
            taxLine.setAmount(taxAmount);
            invoiceLineMapper.insert(taxLine);
        }

        // PASO 9: Actualizar totales
        invoice.setTotal(total);
        invoice.setBalance(total);
        invoiceMapper.update(invoice);
        
        return toDto(invoice);
    }

    @Override
    public Page<InvoiceDto> list(InvoiceFilter f, Pageable p) {
        UUID orderId = f != null ? f.orderId() : null;
        InvoiceStatus status = f != null ? f.status() : null;
        Instant from = f != null ? f.from() : null;
        Instant to = f != null ? f.to() : null;
        List<Invoice> invoices = invoiceMapper.findAll(orderId, status, from, to, p.getPageSize(), p.getOffset());
        long total = invoiceMapper.count(orderId, status, from, to);
        List<InvoiceDto> content = invoices.stream().map(this::toDto).toList();
        return new PageImpl<>(content, p, total);
    }

    @Override
    public InvoiceDto get(UUID id) {
        Invoice invoice = invoiceMapper.findById(id);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        return toDto(invoice);
    }

    @Override
    public InvoiceDto patch(UUID id, InvoicePatchDto dto) {
        Invoice invoice = invoiceMapper.findById(id);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        if (dto.dueDate() != null) invoice.setDueDate(dto.dueDate());
        if (dto.status() != null) invoice.setStatus(dto.status());
        invoiceMapper.update(invoice);
        return toDto(invoice);
    }

    @Override
    public void delete(UUID id) {
        Invoice invoice = invoiceMapper.findById(id);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        invoiceMapper.delete(id);
    }

    @Override
    public Page<InvoiceLineDto> listLines(UUID invoiceId, Pageable p) {
        Invoice invoice = invoiceMapper.findById(invoiceId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        List<InvoiceLine> lines = invoiceLineMapper.findByInvoice(invoiceId, p.getPageSize(), p.getOffset());
        List<InvoiceLineDto> content = lines.stream().map(this::toLineDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public InvoiceLineDto addLine(UUID invoiceId, InvoiceLineCreateDto dto) {
        Invoice invoice = invoiceMapper.findById(invoiceId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        InvoiceLine line = new InvoiceLine();
        line.setInvoiceId(invoiceId);
        line.setType(dto.type());
        line.setReferenceId(dto.referenceId());
        line.setDescription(dto.description());
        line.setAmount(dto.amount());
        invoiceLineMapper.insert(line);
        adjustTotals(invoiceId);
        return toLineDto(line);
    }

    @Override
    public InvoiceLineDto patchLine(UUID invoiceId, LineType tipo, UUID refId, InvoiceLinePatchDto dto) {
        InvoiceLine line = invoiceLineMapper.find(invoiceId, tipo, refId);
        if (line == null) {
            throw new ResourceNotFoundException("Línea no encontrada");
        }
        if (dto.description() != null) line.setDescription(dto.description());
        if (dto.amount() != null) line.setAmount(dto.amount());
        invoiceLineMapper.update(line);
        adjustTotals(invoiceId);
        return toLineDto(line);
    }

    @Override
    public void removeLine(UUID invoiceId, LineType tipo, UUID refId) {
        InvoiceLine line = invoiceLineMapper.find(invoiceId, tipo, refId);
        if (line == null) {
            throw new ResourceNotFoundException("Línea no encontrada");
        }
        invoiceLineMapper.delete(invoiceId, tipo, refId);
        adjustTotals(invoiceId);
    }

    @Override
    public Page<PaymentDto> listPayments(UUID invoiceId, Pageable p) {
        Invoice invoice = invoiceMapper.findById(invoiceId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        List<Payment> payments = paymentMapper.findByInvoice(invoiceId, p.getPageSize(), p.getOffset());
        List<PaymentDto> content = payments.stream().map(this::toPaymentDto).toList();
        return new PageImpl<>(content, p, content.size());
    }

    @Override
    public PaymentDto addPayment(UUID invoiceId, PaymentCreateDto dto) {
        Invoice invoice = invoiceMapper.findById(invoiceId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInvoiceId(invoiceId);
        payment.setAmount(dto.amount());
        payment.setMethod(dto.method());
        payment.setReference(dto.reference());
        payment.setPaymentDate(Instant.now());
        paymentMapper.insert(payment);
        adjustTotals(invoiceId);
        return toPaymentDto(payment);
    }

    @Override
    public void removePayment(UUID invoiceId, UUID pagoId) {
        Invoice invoice = invoiceMapper.findById(invoiceId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Factura no encontrada");
        }
        paymentMapper.delete(invoiceId, pagoId);
        adjustTotals(invoiceId);
    }

    private void adjustTotals(UUID invoiceId) {
        Invoice invoice = invoiceMapper.findById(invoiceId);
        if (invoice == null) {
            return;
        }
        List<InvoiceLine> lines = invoiceLineMapper.findByInvoice(invoiceId, Integer.MAX_VALUE, 0);
        
        // Calcular subtotal (sin impuestos)
        BigDecimal subtotal = lines.stream()
            .filter(line -> !line.getDescription().contains("IVA"))
            .map(InvoiceLine::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Si no hay línea de impuestos y hay subtotal, calcularla
        boolean hasTaxLine = lines.stream().anyMatch(line -> line.getDescription().contains("IVA"));
        BigDecimal total = subtotal;
        
        if (!hasTaxLine && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            // Calcular y agregar impuestos si no existen
            BigDecimal taxRate = new BigDecimal("0.19");
            BigDecimal taxAmount = subtotal.multiply(taxRate);
            total = subtotal.add(taxAmount);
            
            // Crear línea de impuestos si no existe
            InvoiceLine taxLine = new InvoiceLine();
            taxLine.setInvoiceId(invoiceId);
            taxLine.setType(LineType.MANUAL);
            taxLine.setReferenceId(UUID.randomUUID());
            taxLine.setDescription("IVA (19%)");
            taxLine.setAmount(taxAmount);
            invoiceLineMapper.insert(taxLine);
        } else {
            // Sumar todas las líneas incluyendo impuestos
            total = lines.stream().map(InvoiceLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        List<Payment> payments = paymentMapper.findByInvoice(invoiceId, Integer.MAX_VALUE, 0);
        BigDecimal paid = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setTotal(total);
        invoice.setBalance(total.subtract(paid));
        if (invoice.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setBalance(BigDecimal.ZERO);
        }
        invoiceMapper.update(invoice);
    }

    private InvoiceDto toDto(Invoice invoice) {
        return new InvoiceDto(invoice.getId(), invoice.getOrderId(), invoice.getNumber(), invoice.getStatus(), invoice.getIssueDate(), invoice.getDueDate(), invoice.getTotal(), invoice.getBalance());
    }

    private InvoiceLineDto toLineDto(InvoiceLine line) {
        return new InvoiceLineDto(line.getInvoiceId(), line.getType(), line.getReferenceId(), line.getDescription(), line.getAmount());
    }

    private PaymentDto toPaymentDto(Payment payment) {
        return new PaymentDto(payment.getId(), payment.getInvoiceId(), payment.getAmount(), payment.getMethod(), payment.getPaymentDate(), payment.getReference());
    }
}
