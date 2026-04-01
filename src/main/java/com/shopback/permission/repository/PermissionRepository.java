package com.shopback.permission.repository;

import com.shopback.permission.model.Permission;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PermissionRepository {

    private static final RowMapper<Permission> PERMISSION_ROW_MAPPER = (rs, rowNum) -> new Permission(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    public PermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Permission> findAll() {
        return jdbcTemplate.query("""
                select id, code, name, description, created_at
                from permission
                order by id
                """, PERMISSION_ROW_MAPPER);
    }
}
