package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Payment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface PaymentMapper {

    @Select("""
            SELECT id, invoice_id AS invoiceId, amount, method, payment_date AS paymentDate, reference
            FROM invoice_payments
            WHERE invoice_id = #{invoiceId}
            ORDER BY payment_date DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Payment> findByInvoice(@Param("invoiceId") UUID invoiceId,
                                @Param("limit") int limit,
                                @Param("offset") long offset);

    @Insert("""
            INSERT INTO invoice_payments(id, invoice_id, amount, method, payment_date, reference)
            VALUES(#{id}, #{invoiceId}, #{amount}, #{method}, #{paymentDate}, #{reference})
            """)
    void insert(Payment payment);

    @Delete("DELETE FROM invoice_payments WHERE id = #{id} AND invoice_id = #{invoiceId}")
    int delete(@Param("invoiceId") UUID invoiceId, @Param("id") UUID id);
}
