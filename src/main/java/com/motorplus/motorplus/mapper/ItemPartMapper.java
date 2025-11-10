package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.ItemPart;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ItemPartMapper {

    @Select("""
            SELECT order_item_id AS orderItemId, part_id AS partId, quantity, unit_price AS unitPrice
            FROM order_item_parts
            WHERE order_item_id = #{orderItemId}
            ORDER BY part_id
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<ItemPart> findByOrderItem(@Param("orderItemId") UUID orderItemId,
                                   @Param("limit") int limit,
                                   @Param("offset") long offset);

    @Select("""
            SELECT COUNT(*)
            FROM order_item_parts
            WHERE order_item_id = #{orderItemId}
            """)
    long countByOrderItem(@Param("orderItemId") UUID orderItemId);

    @Select("""
            SELECT order_item_id AS orderItemId, part_id AS partId, quantity, unit_price AS unitPrice
            FROM order_item_parts
            WHERE order_item_id = #{orderItemId} AND part_id = #{partId}
            """)
    ItemPart find(@Param("orderItemId") UUID orderItemId, @Param("partId") UUID partId);

    @Insert("""
            INSERT INTO order_item_parts(order_item_id, part_id, quantity, unit_price)
            VALUES(#{orderItemId}, #{partId}, #{quantity}, #{unitPrice})
            """)
    void insert(ItemPart part);

    @Update("""
            UPDATE order_item_parts
            SET quantity = #{quantity},
                unit_price = #{unitPrice}
            WHERE order_item_id = #{orderItemId} AND part_id = #{partId}
            """)
    int update(ItemPart part);

    @Delete("DELETE FROM order_item_parts WHERE order_item_id = #{orderItemId} AND part_id = #{partId}")
    int delete(@Param("orderItemId") UUID orderItemId, @Param("partId") UUID partId);
}
