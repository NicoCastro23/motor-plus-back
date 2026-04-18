package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.PurchaseOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface PurchaseOrderMapper {

    @Select("""
            <script>
            SELECT po.id, po.part_id AS partId, po.supplier_id AS supplierId,
                   po.quantity, po.unit_cost AS unitCost, po.status,
                   po.notes, po.created_at AS createdAt, po.updated_at AS updatedAt
            FROM purchase_orders po
            WHERE 1 = 1
            <if test="partId != null">
                AND po.part_id = #{partId,jdbcType=OTHER}
            </if>
            <if test="status != null">
                AND po.status = #{status}
            </if>
            ORDER BY po.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<PurchaseOrder> findAll(@Param("partId") UUID partId,
                                @Param("status") String status,
                                @Param("limit") int limit,
                                @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM purchase_orders
            WHERE 1 = 1
            <if test="partId != null">
                AND part_id = #{partId,jdbcType=OTHER}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            </script>
            """)
    long count(@Param("partId") UUID partId, @Param("status") String status);

    @Select("""
            SELECT po.id, po.part_id AS partId, po.supplier_id AS supplierId,
                   po.quantity, po.unit_cost AS unitCost, po.status,
                   po.notes, po.created_at AS createdAt, po.updated_at AS updatedAt
            FROM purchase_orders po
            WHERE po.id = #{id}
            """)
    PurchaseOrder findById(@Param("id") UUID id);

    @Select("""
            SELECT po.id, po.part_id AS partId, po.supplier_id AS supplierId,
                   po.quantity, po.unit_cost AS unitCost, po.status,
                   po.notes, po.created_at AS createdAt, po.updated_at AS updatedAt,
                   p.name AS partName, p.sku AS partSku,
                   s.name AS supplierName
            FROM purchase_orders po
            JOIN parts p ON p.id = po.part_id
            LEFT JOIN suppliers s ON s.id = po.supplier_id
            WHERE po.id = #{id}
            """)
    Map<String, Object> findByIdWithDetails(@Param("id") UUID id);

    @Select("""
            <script>
            SELECT po.id, po.part_id AS partId, po.supplier_id AS supplierId,
                   po.quantity, po.unit_cost AS unitCost, po.status,
                   po.notes, po.created_at AS createdAt, po.updated_at AS updatedAt,
                   p.name AS partName, p.sku AS partSku,
                   s.name AS supplierName
            FROM purchase_orders po
            JOIN parts p ON p.id = po.part_id
            LEFT JOIN suppliers s ON s.id = po.supplier_id
            WHERE 1 = 1
            <if test="partId != null">
                AND po.part_id = #{partId,jdbcType=OTHER}
            </if>
            <if test="status != null">
                AND po.status = #{status}
            </if>
            ORDER BY po.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> findAllWithDetails(@Param("partId") UUID partId,
                                                  @Param("status") String status,
                                                  @Param("limit") int limit,
                                                  @Param("offset") long offset);

    @Insert("""
            INSERT INTO purchase_orders(id, part_id, supplier_id, quantity, unit_cost, status, notes, created_at, updated_at)
            VALUES(#{id}, #{partId}, #{supplierId}, #{quantity}, #{unitCost}, #{status}, #{notes}, #{createdAt}, #{updatedAt})
            """)
    void insert(PurchaseOrder order);

    @Update("""
            UPDATE purchase_orders
            SET status = #{status}, unit_cost = #{unitCost}, updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int update(PurchaseOrder order);

    @Select("""
            SELECT EXISTS(
                SELECT 1 FROM purchase_orders
                WHERE part_id = #{partId} AND status IN ('PENDING', 'SENT')
            )
            """)
    boolean existsPendingOrSentForPart(@Param("partId") UUID partId);
}
