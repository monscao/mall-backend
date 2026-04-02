package com.malllite.auth.interceptor;

import com.malllite.auth.annotation.RequireAuth;
import com.malllite.auth.annotation.RequireRole;
import com.malllite.auth.context.AuthContext;
import com.malllite.auth.dto.AuthUser;
import com.malllite.auth.exception.ForbiddenException;
import com.malllite.auth.exception.UnauthorizedException;
import com.malllite.auth.service.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    public AuthInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean authRequired = requiresAuth(handlerMethod);
        String[] requiredRoles = requiredRoles(handlerMethod);
        if (!authRequired && requiredRoles.length == 0) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        AuthUser authUser = jwtTokenService.parseToken(token);
        AuthContext.setCurrentUser(authUser);

        if (requiredRoles.length > 0 && Arrays.stream(requiredRoles).noneMatch(authUser.roleCodes()::contains)) {
            throw new ForbiddenException("Insufficient permissions");
        }

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        AuthContext.clear();
    }

    private boolean requiresAuth(HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().isAnnotationPresent(RequireAuth.class)
                || handlerMethod.hasMethodAnnotation(RequireAuth.class);
    }

    private String[] requiredRoles(HandlerMethod handlerMethod) {
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }

        RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        return classAnnotation != null ? classAnnotation.value() : new String[0];
    }
}
