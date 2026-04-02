package com.malllite.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCheckRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCheckRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCheckRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String database = jdbcTemplate.queryForObject("select current_database()", String.class);
        Integer userCount = jdbcTemplate.queryForObject("select count(*) from app_user", Integer.class);
        Integer permissionCount = jdbcTemplate.queryForObject("select count(*) from permission", Integer.class);
        Integer roleCount = jdbcTemplate.queryForObject("select count(*) from role", Integer.class);
        log.info("Connected to database: {}", database);
        log.info("Current app_user rows: {}", userCount);
        log.info("Current permission rows: {}", permissionCount);
        log.info("Current role rows: {}", roleCount);
    }
}
