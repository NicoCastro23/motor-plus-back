package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.dto.ordersDtos.OrderStatus;
import com.motorplus.motorplus.model.Order;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface OrderMapper {

    @Select("""
            <script>
            SELECT id, client_id AS clientId, license_plate AS licensePlate, status, description, total, created_at AS createdAt, updated_at AS updatedAt
            FROM orders
            WHERE 1 = 1
            <if test="clientId != null">
                AND client_id = #{clientId}
            </if>
            <if test="licensePlate != null and licensePlate != ''">
                AND license_plate = #{licensePlate}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="from != null">
                AND created_at &gt;= #{from}
            </if>
            <if test="to != null">
                AND created_at &lt;= #{to}
            </if>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Order> findAll(@Param("clientId") UUID clientId,
                        @Param("licensePlate") String licensePlate,
                        @Param("status") OrderStatus status,
                        @Param("from") Instant from,
                        @Param("to") Instant to,
                        @Param("limit") int limit,
                        @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM orders
            WHERE 1 = 1
            <if test="clientId != null">
                AND client_id = #{clientId}
            </if>
            <if test="licensePlate != null and licensePlate != ''">
                AND license_plate = #{licensePlate}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="from != null">
                AND created_at &gt;= #{from}
            </if>
            <if test="to != null">
                AND created_at &lt;= #{to}
            </if>
            </script>
            """)
    long count(@Param("clientId") UUID clientId,
               @Param("licensePlate") String licensePlate,
               @Param("status") OrderStatus status,
               @Param("from") Instant from,
               @Param("to") Instant to);

    @Select("""
            SELECT id, client_id AS clientId, license_plate AS licensePlate, status, description, total, created_at AS createdAt, updated_at AS updatedAt
            FROM orders
            WHERE id = #{id}
            """)
    Order findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO orders(id, client_id, license_plate, status, description, total, created_at, updated_at)
            VALUES(#{id}, #{clientId}, #{licensePlate}, #{status}, #{description}, #{total}, #{createdAt}, #{updatedAt})
            """)
    void insert(Order order);

    @Update("""
            UPDATE orders
            SET license_plate = #{licensePlate},
                description = #{description},
                status = #{status},
                total = #{total},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int update(Order order);

    @Update("""
            UPDATE orders
            SET status = #{status},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") UUID id, @Param("status") OrderStatus status, @Param("updatedAt") Instant updatedAt);

    @Delete("DELETE FROM orders WHERE id = #{id}")
    int delete(@Param("id") UUID id);
}
