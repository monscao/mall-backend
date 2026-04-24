package com.malllite.auth.config;

import com.malllite.agent.interceptor.AgentRateLimitInterceptor;
import com.malllite.auth.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AgentRateLimitInterceptor agentRateLimitInterceptor;
    private final String uploadDirectory;

    public WebMvcConfig(
            AuthInterceptor authInterceptor,
            AgentRateLimitInterceptor agentRateLimitInterceptor,
            @Value("${app.upload-dir:uploads}") String uploadDirectory
    ) {
        this.authInterceptor = authInterceptor;
        this.agentRateLimitInterceptor = agentRateLimitInterceptor;
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(agentRateLimitInterceptor).addPathPatterns("/api/agent/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
