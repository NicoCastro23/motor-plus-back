package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.SupplierPart;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SupplierPartMapper {

    @Select("""
            SELECT supplier_id AS supplierId, part_id AS partId, price, min_quantity AS minQuantity
            FROM supplier_parts
            WHERE supplier_id = #{supplierId}
            ORDER BY part_id
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<SupplierPart> findBySupplier(@Param("supplierId") UUID supplierId,
                                      @Param("limit") int limit,
                                      @Param("offset") long offset);

    @Select("""
            SELECT COUNT(*)
            FROM supplier_parts
            WHERE supplier_id = #{supplierId}
            """)
    long countBySupplier(@Param("supplierId") UUID supplierId);

    @Select("""
            SELECT supplier_id AS supplierId, part_id AS partId, price, min_quantity AS minQuantity
            FROM supplier_parts
            WHERE supplier_id = #{supplierId} AND part_id = #{partId}
            """)
    SupplierPart find(@Param("supplierId") UUID supplierId, @Param("partId") UUID partId);

    @Insert("""
            INSERT INTO supplier_parts(supplier_id, part_id, price, min_quantity)
            VALUES(#{supplierId}, #{partId}, #{price}, #{minQuantity})
            """)
    void insert(SupplierPart supplierPart);

    @Update("""
            UPDATE supplier_parts
            SET price = #{price},
                min_quantity = #{minQuantity}
            WHERE supplier_id = #{supplierId} AND part_id = #{partId}
            """)
    int update(SupplierPart supplierPart);

    @Delete("DELETE FROM supplier_parts WHERE supplier_id = #{supplierId} AND part_id = #{partId}")
    int delete(@Param("supplierId") UUID supplierId, @Param("partId") UUID partId);
}
