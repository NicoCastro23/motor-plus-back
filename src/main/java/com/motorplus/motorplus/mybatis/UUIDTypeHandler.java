package com.motorplus.motorplus.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.UUID;

@MappedTypes(UUID.class)
@MappedJdbcTypes(JdbcType.OTHER) // Postgres UUID
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter); // el driver de Postgres acepta UUID vía setObject
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object v = rs.getObject(columnName);
        if (v == null) {
            return null;
        }
        if (v instanceof UUID) {
            return (UUID) v;
        }
        if (v instanceof String) {
            try {
                return UUID.fromString((String) v);
            } catch (IllegalArgumentException e) {
                throw new SQLException("Invalid UUID string: " + v, e);
            }
        }
        // Si es otro tipo (como Long, Integer), intentar convertirlo
        try {
            return UUID.fromString(v.toString());
        } catch (IllegalArgumentException e) {
            throw new SQLException("Cannot convert value to UUID: " + v + " (type: " + v.getClass().getName() + ")", e);
        }
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object v = rs.getObject(columnIndex);
        if (v == null) {
            return null;
        }
        if (v instanceof UUID) {
            return (UUID) v;
        }
        if (v instanceof String) {
            try {
                return UUID.fromString((String) v);
            } catch (IllegalArgumentException e) {
                throw new SQLException("Invalid UUID string: " + v, e);
            }
        }
        // Si es otro tipo (como Long, Integer), intentar convertirlo
        try {
            return UUID.fromString(v.toString());
        } catch (IllegalArgumentException e) {
            throw new SQLException("Cannot convert value to UUID: " + v + " (type: " + v.getClass().getName() + ")", e);
        }
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object v = cs.getObject(columnIndex);
        if (v == null) {
            return null;
        }
        if (v instanceof UUID) {
            return (UUID) v;
        }
        if (v instanceof String) {
            try {
                return UUID.fromString((String) v);
            } catch (IllegalArgumentException e) {
                throw new SQLException("Invalid UUID string: " + v, e);
            }
        }
        // Si es otro tipo (como Long, Integer), intentar convertirlo
        try {
            return UUID.fromString(v.toString());
        } catch (IllegalArgumentException e) {
            throw new SQLException("Cannot convert value to UUID: " + v + " (type: " + v.getClass().getName() + ")", e);
        }
    }
}
