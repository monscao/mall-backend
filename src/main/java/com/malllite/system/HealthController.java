package com.malllite.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class HealthController {

    private final String backendVersion;
    private final String frontendVersion;

    public HealthController(
            @Value("${app.version:0.0.1-SNAPSHOT}") String backendVersion,
            @Value("${app.frontend-version:0.1.0}") String frontendVersion
    ) {
        this.backendVersion = backendVersion;
        this.frontendVersion = frontendVersion;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "mall-backend",
                "backendVersion", backendVersion,
                "frontendVersion", frontendVersion
        );
    }
}
