package com.ashanhimantha.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // --- Public Read-Only Access ---
                        // Rule: Anyone (authenticated or not) can view the list of categories and products.
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()


                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        // Configure the JWT converter to correctly process Cognito roles
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /**
     * This bean defines the converter that reads the JWT and extracts authorities.
     * It combines the standard authorities converter with our custom group converter.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        Converter<Jwt, Collection<GrantedAuthority>> groupAuthoritiesConverter = new CognitoGroupAuthoritiesConverter();

        // Combine the converters. The final list of authorities will contain both standard
        // OAuth scopes and our custom Cognito groups prefixed with ROLE_.
        Converter<Jwt, Collection<GrantedAuthority>> combinedConverter = jwt ->
                Stream.concat(
                        grantedAuthoritiesConverter.convert(jwt).stream(),
                        groupAuthoritiesConverter.convert(jwt).stream()
                ).collect(Collectors.toList());

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