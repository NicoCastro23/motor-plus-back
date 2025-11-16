package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.dto.invoiceDtos.InvoiceStatus;
import com.motorplus.motorplus.model.Invoice;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface InvoiceMapper {

    @Select("""
            <script>
            SELECT id, order_id AS orderId, number, status, issue_date AS issueDate, due_date AS dueDate, total, balance
            FROM invoices
            WHERE 1 = 1
            <if test="orderId != null">
                AND order_id = #{orderId}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="from != null">
                AND issue_date &gt;= #{from}
            </if>
            <if test="to != null">
                AND issue_date &lt;= #{to}
            </if>
            ORDER BY issue_date DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Invoice> findAll(@Param("orderId") UUID orderId,
                          @Param("status") InvoiceStatus status,
                          @Param("from") Instant from,
                          @Param("to") Instant to,
                          @Param("limit") int limit,
                          @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM invoices
            WHERE 1 = 1
            <if test="orderId != null">
                AND order_id = #{orderId}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="from != null">
                AND issue_date &gt;= #{from}
            </if>
            <if test="to != null">
                AND issue_date &lt;= #{to}
            </if>
            </script>
            """)
    long count(@Param("orderId") UUID orderId,
               @Param("status") InvoiceStatus status,
               @Param("from") Instant from,
               @Param("to") Instant to);

    @Select("""
            SELECT id, order_id AS orderId, number, status, issue_date AS issueDate, due_date AS dueDate, total, balance
            FROM invoices
            WHERE id = #{id}
            """)
    Invoice findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO invoices(id, order_id, number, status, issue_date, due_date, total, balance)
            VALUES(#{id}, #{orderId}, #{number}, #{status}, #{issueDate}, #{dueDate}, #{total}, #{balance})
            """)
    void insert(Invoice invoice);

    @Update("""
            UPDATE invoices
            SET status = #{status},
                total = #{total},
                balance = #{balance},
                due_date = #{dueDate}
            WHERE id = #{id}
            """)
    int update(Invoice invoice);

    @Delete("DELETE FROM invoices WHERE id = #{id}")
    int delete(@Param("id") UUID id);
}
