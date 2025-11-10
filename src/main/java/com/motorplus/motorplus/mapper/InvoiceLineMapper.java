package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.dto.invoiceDtos.LineType;
import com.motorplus.motorplus.model.InvoiceLine;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface InvoiceLineMapper {

    @Select("""
            SELECT invoice_id AS invoiceId, type, reference_id AS referenceId, description, amount
            FROM invoice_lines
            WHERE invoice_id = #{invoiceId}
            ORDER BY created_at
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<InvoiceLine> findByInvoice(@Param("invoiceId") UUID invoiceId,
                                    @Param("limit") int limit,
                                    @Param("offset") long offset);

    @Select("""
            SELECT invoice_id AS invoiceId, type, reference_id AS referenceId, description, amount
            FROM invoice_lines
            WHERE invoice_id = #{invoiceId} AND type = #{type} AND reference_id = #{referenceId}
            """)
    InvoiceLine find(@Param("invoiceId") UUID invoiceId,
                     @Param("type") LineType type,
                     @Param("referenceId") UUID referenceId);

    @Insert("""
            INSERT INTO invoice_lines(invoice_id, type, reference_id, description, amount)
            VALUES(#{invoiceId}, #{type}, #{referenceId}, #{description}, #{amount})
            """)
    void insert(InvoiceLine line);

    @Update("""
            UPDATE invoice_lines
            SET description = #{description},
                amount = #{amount}
            WHERE invoice_id = #{invoiceId} AND type = #{type} AND reference_id = #{referenceId}
            """)
    int update(InvoiceLine line);

    @Delete("DELETE FROM invoice_lines WHERE invoice_id = #{invoiceId} AND type = #{type} AND reference_id = #{referenceId}")
    int delete(@Param("invoiceId") UUID invoiceId,
               @Param("type") LineType type,
               @Param("referenceId") UUID referenceId);
}
