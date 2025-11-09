package com.ashanhimantha.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Essential for @PreAuthorize to work
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        // First filter chain for public GET endpoints only - NO JWT validation
        http.securityMatcher(request -> {
                    String path = request.getServletPath();
                    String method = request.getMethod();

                    // Allow Swagger/OpenAPI endpoints
                    if (path.startsWith("/swagger-ui") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/api-docs") ||
                        path.equals("/swagger-ui.html")) {
                        return true;
                    }

                    return "GET".equals(method) &&
                           !path.contains("/admin") && // Exclude any admin endpoints
                           (path.startsWith("/api/v1/categories") ||
                            path.startsWith("/api/v1/products") ||
                            path.startsWith("/api/v1/category-types"));
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Swagger UI and OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/category-types/**").permitAll()
                        .anyRequest().denyAll() // This should never be reached due to securityMatcher
                );
        // No OAuth2 configuration here - completely bypasses JWT validation

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain protectedSecurityFilterChain(HttpSecurity http) throws Exception {
        // Second filter chain for all other endpoints - WITH JWT validation
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        Converter<Jwt, Collection<GrantedAuthority>> groupAuthoritiesConverter = new CognitoGroupAuthoritiesConverter();

        // Combine the converters. The final list of authorities will contain both standard
        // OAuth scopes and our custom Cognito groups prefixed with ROLE_.
        Converter<Jwt, Collection<GrantedAuthority>> combinedConverter = jwt -> {
            Collection<GrantedAuthority> standardAuthorities = grantedAuthoritiesConverter.convert(jwt);
            Collection<GrantedAuthority> groupAuthorities = groupAuthoritiesConverter.convert(jwt);

            return Stream.concat(
                    standardAuthorities.stream(),
                    groupAuthorities != null ? groupAuthorities.stream() : Stream.empty()
            ).collect(Collectors.toList());
        };

        converter.setJwtGrantedAuthoritiesConverter(combinedConverter);
        return converter;
    }

    /**
     * Custom converter to extract the 'cognito:groups' claim from the JWT
     * and map them to Spring Security authorities with a 'ROLE_' prefix.
     * This allows us to use hasRole('SuperAdmins') in @PreAuthorize.
     */
    static class CognitoGroupAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            if (jwt.hasClaim("cognito:groups")) {
                List<String> groups = jwt.getClaimAsStringList("cognito:groups");
                // This is the critical line. It adds "ROLE_" to each group name.
                return groups.stream()
                        .map(group -> new SimpleGrantedAuthority("ROLE_" + group))
                        .collect(Collectors.toList());
            }
            return List.of(); // Return empty list if no groups claim
        }
    }
}
