package com.ashanhimantha.product_service.config; // Changed package name

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // --- THIS IS THE SECTION WE ARE MODIFYING ---
                .authorizeHttpRequests(authorize -> authorize
                        // Allow ANYONE to VIEW products and categories
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                        // Require 'Suppliers' or 'SuperAdmins' for write access to PRODUCTS
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyAuthority("Suppliers", "SuperAdmins")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyAuthority("Suppliers", "SuperAdmins")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyAuthority("Suppliers", "SuperAdmins")

                        // Require 'SuperAdmins' for write access to CATEGORIES
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasAuthority("SuperAdmins")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasAuthority("SuperAdmins")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasAuthority("SuperAdmins")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler())
                );
        return http.build();
    }

    // --- ALL THE BEANS BELOW CAN REMAIN EXACTLY THE SAME ---

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // ... (This code is perfect, no changes needed)
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        // ... (This code is perfect, no changes needed)
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               AccessDeniedException accessDeniedException) throws IOException {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"You do not have permission to access this resource\"}");
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // ... (This code is perfect, no changes needed)
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        // IMPORTANT CHANGE: Use hasAuthority() instead of hasRole()
        return jwt -> {
            Object groupsClaim = jwt.getClaim("cognito:groups");
            if (groupsClaim instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> groups = (List<String>) groupsClaim;
                // DO NOT add the "ROLE_" prefix here
                return groups.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
            return List.of();
        };
    }
}