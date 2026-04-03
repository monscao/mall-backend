package com.malllite.user.repository;

import com.malllite.user.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("nickname"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getBoolean("enabled"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcClient jdbcClient;

    public UserRepository(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            JdbcClient jdbcClient
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcClient = jdbcClient;
    }

    public List<User> findAll() {
        return jdbcTemplate.query("""
                select id, username, password_hash, nickname, email, phone, enabled, created_at, updated_at
                from app_user
                order by id
                """, USER_ROW_MAPPER);
    }

    public Optional<User> findByUsername(String username) {
        return jdbcClient.sql("""
                        select id, username, password_hash, nickname, email, phone, enabled, created_at, updated_at
                        from app_user
                        where username = :username
                        """)
                .param("username", username)
                .query(USER_ROW_MAPPER)
                .optional();
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcClient.sql("""
                        select count(*)
                        from app_user
                        where username = :username
                        """)
                .param("username", username)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    public User createUser(String username, String passwordHash, String nickname, String email, String phone) {
        return jdbcClient.sql("""
                        insert into app_user (username, password_hash, nickname, email, phone)
                        values (:username, :passwordHash, :nickname, :email, :phone)
                        returning id, username, password_hash, nickname, email, phone, enabled, created_at, updated_at
                        """)
                .param("username", username)
                .param("passwordHash", passwordHash)
                .param("nickname", nickname)
                .param("email", email)
                .param("phone", phone)
                .query(USER_ROW_MAPPER)
                .single();
    }

    public void assignRoleByCode(Long userId, String roleCode) {
        jdbcClient.sql("""
                        insert into user_role (user_id, role_id)
                        select :userId, r.id
                        from role r
                        where r.code = :roleCode
                        on conflict (user_id, role_id) do nothing
                        """)
                .param("userId", userId)
                .param("roleCode", roleCode)
                .update();
    }

    public Map<Long, List<String>> findRoleCodesByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<String>> result = new HashMap<>();
        namedParameterJdbcTemplate.query("""
                select ur.user_id, r.code
                from user_role ur
                join role r on r.id = ur.role_id
                where ur.user_id in (:userIds)
                order by ur.user_id, r.code
                """, new MapSqlParameterSource("userIds", userIds),
                rs -> {
                    Long userId = rs.getLong("user_id");
                    result.computeIfAbsent(userId, ignored -> new ArrayList<>())
                            .add(rs.getString("code"));
                });
        return result;
    }

    public Map<Long, List<String>> findPermissionCodesByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<String>> result = new HashMap<>();
        namedParameterJdbcTemplate.query("""
                select distinct access.user_id, access.code
                from (
                    select up.user_id, p.code
                    from user_permission up
                    join permission p on p.id = up.permission_id
                    where up.user_id in (:userIds)
                    union
                    select ur.user_id, p.code
                    from user_role ur
                    join role_permission rp on rp.role_id = ur.role_id
                    join permission p on p.id = rp.permission_id
                    where ur.user_id in (:userIds)
                ) access
                order by access.user_id, access.code
                """, new MapSqlParameterSource("userIds", userIds),
                rs -> {
                    Long userId = rs.getLong("user_id");
                    result.computeIfAbsent(userId, ignored -> new ArrayList<>())
                            .add(rs.getString("code"));
                });
        return result;
    }
}
