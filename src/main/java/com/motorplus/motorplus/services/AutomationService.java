package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.purchaseOrderDtos.PurchaseOrderStatus;
import com.motorplus.motorplus.mapper.InvoiceMapper;
import com.motorplus.motorplus.mapper.OrderMapper;
import com.motorplus.motorplus.mapper.PartMapper;
import com.motorplus.motorplus.mapper.PurchaseOrderMapper;
import com.motorplus.motorplus.mapper.StockAlertMapper;
import com.motorplus.motorplus.mapper.SupplierMapper;
import com.motorplus.motorplus.model.Invoice;
import com.motorplus.motorplus.model.Order;
import com.motorplus.motorplus.model.Part;
import com.motorplus.motorplus.model.PurchaseOrder;
import com.motorplus.motorplus.model.StockAlert;
import com.motorplus.motorplus.model.Supplier;
import com.motorplus.motorplus.dto.invoiceDtos.InvoiceStatus;
import com.motorplus.motorplus.dto.ordersDtos.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de automatización con tareas programadas
 */
@Service
public class AutomationService {

    private static final Logger logger = LoggerFactory.getLogger(AutomationService.class);
    
    private final PartMapper partMapper;
    private final InvoiceMapper invoiceMapper;
    private final OrderMapper orderMapper;
    private final StockAlertMapper stockAlertMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SupplierMapper supplierMapper;

    private static final int STALE_ORDER_DAYS = 7;
    private static final int DRAFT_EXPIRATION_DAYS = 30;

    public AutomationService(PartMapper partMapper, InvoiceMapper invoiceMapper,
                             OrderMapper orderMapper, StockAlertMapper stockAlertMapper,
                             PurchaseOrderMapper purchaseOrderMapper, SupplierMapper supplierMapper) {
        this.partMapper = partMapper;
        this.invoiceMapper = invoiceMapper;
        this.orderMapper = orderMapper;
        this.stockAlertMapper = stockAlertMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.supplierMapper = supplierMapper;
    }

    /**
     * Verifica repuestos con stock bajo y genera alertas
     * Se ejecuta diariamente a las 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * ?") // 8:00 AM todos los días
    @Transactional
    public void checkLowStock() {
        logger.info("Iniciando verificación de stock bajo...");
        try {
            List<Part> parts = partMapper.findAll(null, true, Integer.MAX_VALUE, 0);
            int lowStockCount = 0;

            for (Part part : parts) {
                if (part.getStock() < part.getMinStock()) {
                    lowStockCount++;
                    logger.warn("STOCK BAJO: {} (SKU: {}) - Stock: {}/{}", part.getName(), part.getSku(), part.getStock(), part.getMinStock());
                    if (!stockAlertMapper.existsActive(part.getId())) {
                        StockAlert alert = new StockAlert();
                        alert.setId(UUID.randomUUID());
                        alert.setPartId(part.getId());
                        alert.setCurrentStock(part.getStock());
                        alert.setMinStock(part.getMinStock());
                        alert.setCreatedAt(Instant.now());
                        alert.setResolved(false);
                        stockAlertMapper.insert(alert);
                    }
                    if (!purchaseOrderMapper.existsPendingOrSentForPart(part.getId())) {
                        Supplier supplier = supplierMapper.findFirstByPartId(part.getId());
                        PurchaseOrder po = new PurchaseOrder();
                        po.setId(UUID.randomUUID());
                        po.setPartId(part.getId());
                        po.setSupplierId(supplier != null ? supplier.getId() : null);
                        po.setQuantity(part.getMinStock() * 2);
                        po.setStatus(PurchaseOrderStatus.PENDING.name());
                        po.setNotes("Generado automáticamente. Stock actual: " + part.getStock() + " / mínimo: " + part.getMinStock());
                        po.setCreatedAt(Instant.now());
                        po.setUpdatedAt(Instant.now());
                        purchaseOrderMapper.insert(po);
                        logger.info("Orden de compra automática creada para {} (cantidad: {})", part.getName(), po.getQuantity());
                    }
                } else {
                    stockAlertMapper.resolveByPart(part.getId(), Instant.now());
                }
            }

            if (lowStockCount > 0) {
                logger.warn("Se encontraron {} repuestos con stock bajo", lowStockCount);
            } else {
                logger.info("Todos los repuestos tienen stock suficiente");
            }
        } catch (Exception e) {
            logger.error("Error al verificar stock bajo", e);
        }
    }

    /**
     * Identifica facturas vencidas
     * Se ejecuta diariamente a las 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?") // 9:00 AM todos los días
    @Transactional(readOnly = true)
    public void checkOverdueInvoices() {
        logger.info("Iniciando verificación de facturas vencidas...");
        
        try {
            List<Invoice> invoices = invoiceMapper.findAll(null, InvoiceStatus.ISSUED, null, null, Integer.MAX_VALUE, 0);
            Instant now = Instant.now();
            int overdueCount = 0;
            
            for (Invoice invoice : invoices) {
                if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(now)) {
                    overdueCount++;
                    long daysOverdue = (now.toEpochMilli() - invoice.getDueDate().toEpochMilli()) / (1000 * 60 * 60 * 24);
                    logger.warn("⚠️ FACTURA VENCIDA: {} - Vencida hace {} días - Balance: ${}", 
                        invoice.getNumber(), daysOverdue, invoice.getBalance());
                }
            }
            
            if (overdueCount > 0) {
                logger.warn("Se encontraron {} facturas vencidas", overdueCount);
            } else {
                logger.info("✅ No hay facturas vencidas");
            }
        } catch (Exception e) {
            logger.error("Error al verificar facturas vencidas", e);
        }
    }

    /**
     * Identifica órdenes estancadas (en progreso por mucho tiempo)
     * Se ejecuta semanalmente los lunes a las 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * MON") // Lunes a las 8:00 AM
    @Transactional(readOnly = true)
    public void checkStaleOrders() {
        logger.info("Iniciando verificación de órdenes estancadas...");
        
        try {
            List<Order> orders = orderMapper.findAll(null, null, OrderStatus.IN_PROGRESS, null, null, Integer.MAX_VALUE, 0);
            Instant threshold = Instant.now().minusSeconds(STALE_ORDER_DAYS * 24 * 3600);
            int staleCount = 0;
            
            for (Order order : orders) {
                if (order.getUpdatedAt().isBefore(threshold)) {
                    staleCount++;
                    long daysStale = (Instant.now().toEpochMilli() - order.getUpdatedAt().toEpochMilli()) / (1000 * 60 * 60 * 24);
                    logger.warn("⚠️ ORDEN ESTANCADA: {} - En progreso hace {} días - Placa: {}", 
                        order.getId(), daysStale, order.getLicensePlate());
                }
            }
            
            if (staleCount > 0) {
                logger.warn("Se encontraron {} órdenes estancadas (más de {} días sin actualizar)", 
                    staleCount, STALE_ORDER_DAYS);
            } else {
                logger.info("✅ No hay órdenes estancadas");
            }
        } catch (Exception e) {
            logger.error("Error al verificar órdenes estancadas", e);
        }
    }

    /**
     * Cancela automáticamente borradores antiguos
     * Se ejecuta semanalmente los domingos a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN") // Domingo a las 2:00 AM
    @Transactional
    public void cancelOldDrafts() {
        logger.info("Iniciando cancelación de borradores antiguos...");
        
        try {
            List<Order> drafts = orderMapper.findAll(null, null, OrderStatus.DRAFT, null, null, Integer.MAX_VALUE, 0);
            Instant threshold = Instant.now().minusSeconds(DRAFT_EXPIRATION_DAYS * 24 * 3600);
            int cancelledCount = 0;
            
            for (Order order : drafts) {
                if (order.getUpdatedAt().isBefore(threshold)) {
                    orderMapper.updateStatus(order.getId(), OrderStatus.CANCELLED, Instant.now());
                    cancelledCount++;
                    logger.info("✅ Borrador cancelado automáticamente: {} - Antigüedad: {} días", 
                        order.getId(), 
                        (Instant.now().toEpochMilli() - order.getUpdatedAt().toEpochMilli()) / (1000 * 60 * 60 * 24));
                }
            }
            
            if (cancelledCount > 0) {
                logger.info("Se cancelaron {} borradores antiguos (más de {} días sin actualizar)", 
                    cancelledCount, DRAFT_EXPIRATION_DAYS);
            } else {
                logger.info("✅ No hay borradores antiguos para cancelar");
            }
        } catch (Exception e) {
            logger.error("Error al cancelar borradores antiguos", e);
        }
    }
}

