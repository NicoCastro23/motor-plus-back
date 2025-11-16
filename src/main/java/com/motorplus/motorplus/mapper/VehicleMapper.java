package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Vehicle;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface VehicleMapper {

    @Select("""
            <script>
            SELECT v.id, v.client_id AS clientId, v.brand, v.model, v.license_plate AS licensePlate, 
                   v.model_year AS modelYear, v.created_at AS createdAt,
                   CONCAT(c.first_name, ' ', c.last_name) AS clientName
            FROM vehicles v
            LEFT JOIN clients c ON v.client_id = c.id
            WHERE 1 = 1
            <if test="brand != null and brand != ''">
                AND LOWER(v.brand) LIKE LOWER(CONCAT('%', #{brand}, '%'))
            </if>
            <if test="model != null and model != ''">
                AND LOWER(v.model) LIKE LOWER(CONCAT('%', #{model}, '%'))
            </if>
            <if test="licensePlate != null and licensePlate != ''">
                AND LOWER(v.license_plate) LIKE LOWER(CONCAT('%', #{licensePlate}, '%'))
            </if>
            ORDER BY v.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Vehicle> findAll(@Param("brand") String brand,
                          @Param("model") String model,
                          @Param("licensePlate") String licensePlate,
                          @Param("limit") int limit,
                          @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM vehicles
            WHERE 1 = 1
            <if test="brand != null and brand != ''">
                AND LOWER(brand) LIKE LOWER(CONCAT('%', #{brand}, '%'))
            </if>
            <if test="model != null and model != ''">
                AND LOWER(model) LIKE LOWER(CONCAT('%', #{model}, '%'))
            </if>
            <if test="licensePlate != null and licensePlate != ''">
                AND LOWER(license_plate) LIKE LOWER(CONCAT('%', #{licensePlate}, '%'))
            </if>
            </script>
            """)
    long count(@Param("brand") String brand,
               @Param("model") String model,
               @Param("licensePlate") String licensePlate);

    @Select("""
            SELECT v.id, v.client_id AS clientId, v.brand, v.model, v.license_plate AS licensePlate, 
                   v.model_year AS modelYear, v.created_at AS createdAt,
                   CONCAT(c.first_name, ' ', c.last_name) AS clientName
            FROM vehicles v
            LEFT JOIN clients c ON v.client_id = c.id
            WHERE v.license_plate = #{licensePlate}
            """)
    Vehicle findByLicense(@Param("licensePlate") String licensePlate);

    @Select("""
            SELECT id, client_id AS clientId, brand, model, license_plate AS licensePlate, model_year AS modelYear, created_at AS createdAt
            FROM vehicles
            WHERE id = #{id}
            """)
    Vehicle findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO vehicles(id, client_id, brand, model, license_plate, model_year, created_at)
            VALUES(#{id}, #{clientId}, #{brand}, #{model}, #{licensePlate}, #{modelYear}, #{createdAt})
            """)
    void insert(Vehicle vehicle);

    @Update("""
            UPDATE vehicles
            SET brand = #{brand},
                model = #{model},
                license_plate = #{licensePlate},
                model_year = #{modelYear},
                client_id = #{clientId}
            WHERE id = #{id}
            """)
    int update(Vehicle vehicle);
    
    @Update("""
            UPDATE vehicles
            SET client_id = #{clientId}
            WHERE license_plate = #{licensePlate}
            """)
    int updateClientId(@Param("licensePlate") String licensePlate, @Param("clientId") UUID clientId);

    @Delete("DELETE FROM vehicles WHERE license_plate = #{licensePlate}")
    int delete(@Param("licensePlate") String licensePlate);

    @Select("""
            SELECT id, client_id AS clientId, brand, model, license_plate AS licensePlate, model_year AS modelYear, created_at AS createdAt
            FROM vehicles
            WHERE client_id = #{clientId}
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Vehicle> findByClient(@Param("clientId") UUID clientId,
                               @Param("limit") int limit,
                               @Param("offset") long offset);

    @Select("""
            SELECT COUNT(*)
            FROM vehicles
            WHERE client_id = #{clientId}
            """)
    long countByClient(@Param("clientId") UUID clientId);
}
