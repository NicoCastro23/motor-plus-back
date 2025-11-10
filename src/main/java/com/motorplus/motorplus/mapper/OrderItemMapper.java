package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface OrderItemMapper {

    @Select("""
            SELECT id, order_id AS orderId, service_id AS serviceId, description, quantity, unit_price AS unitPrice
            FROM order_items
            WHERE order_id = #{orderId}
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<OrderItem> findByOrder(@Param("orderId") UUID orderId,
                                @Param("limit") int limit,
                                @Param("offset") long offset);

    @Select("""
            SELECT COUNT(*)
            FROM order_items
            WHERE order_id = #{orderId}
            """)
    long countByOrder(@Param("orderId") UUID orderId);

    @Select("""
            SELECT id, order_id AS orderId, service_id AS serviceId, description, quantity, unit_price AS unitPrice
            FROM order_items
            WHERE id = #{id} AND order_id = #{orderId}
            """)
    OrderItem findById(@Param("orderId") UUID orderId, @Param("id") UUID id);

    @Insert("""
            INSERT INTO order_items(id, order_id, service_id, description, quantity, unit_price)
            VALUES(#{id}, #{orderId}, #{serviceId}, #{description}, #{quantity}, #{unitPrice})
            """)
    void insert(OrderItem item);

    @Update("""
            UPDATE order_items
            SET service_id = #{serviceId},
                description = #{description},
                quantity = #{quantity},
                unit_price = #{unitPrice}
            WHERE id = #{id} AND order_id = #{orderId}
            """)
    int update(OrderItem item);

    @Delete("DELETE FROM order_items WHERE id = #{id} AND order_id = #{orderId}")
    int delete(@Param("orderId") UUID orderId, @Param("id") UUID id);
}
