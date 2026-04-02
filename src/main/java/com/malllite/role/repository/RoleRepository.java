package com.malllite.role.repository;

import com.malllite.role.model.Role;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RoleRepository {

    private static final RowMapper<Role> ROLE_ROW_MAPPER = (rs, rowNum) -> new Role(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public RoleRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<Role> findAll() {
        return namedParameterJdbcTemplate.getJdbcTemplate().query("""
                select id, code, name, description, created_at
                from role
                order by id
                """, ROLE_ROW_MAPPER);
    }

    public Map<Long, List<String>> findPermissionCodesByRoleIds(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<String>> result = new HashMap<>();
        namedParameterJdbcTemplate.query("""
                select rp.role_id, p.code
                from role_permission rp
                join permission p on p.id = rp.permission_id
                where rp.role_id in (:roleIds)
                order by rp.role_id, p.code
                """, new MapSqlParameterSource("roleIds", roleIds),
                rs -> {
                    Long roleId = rs.getLong("role_id");
                    result.computeIfAbsent(roleId, ignored -> new ArrayList<>())
                            .add(rs.getString("code"));
                });
        return result;
    }
}
