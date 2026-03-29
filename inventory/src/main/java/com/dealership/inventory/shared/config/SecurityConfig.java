package com.dealership.inventory.shared.config;

import com.dealership.inventory.shared.security.TenantFilter;
import com.dealership.inventory.shared.security.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the Inventory module.
 *
 * <p>Uses HTTP Basic for simplicity. In production, replace with JWT / OAuth2.
 *
 * <p>In-memory users are defined here for demonstration. Replace with a
 * real {@link UserDetailsService} backed by a database or identity provider.
 *
 * <p>The {@link TenantFilter} is inserted BEFORE the
 * {@link UsernamePasswordAuthenticationFilter} so that tenant context is
 * available throughout the security filter chain.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TenantFilter tenantFilter;

    public SecurityConfig(TenantFilter tenantFilter) {
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/admin/**").hasRole(UserRole.GLOBAL_ADMIN.name())
                .anyRequest().hasAnyRole(
                        UserRole.TENANT_USER.name(),
                        UserRole.GLOBAL_ADMIN.name()
                )
            )
            .httpBasic(basic -> {});

        return http.build();
    }

    /**
     * Demo in-memory users.
     * <pre>
     *   tenant-user  / secret  → TENANT_USER
     *   global-admin / secret  → GLOBAL_ADMIN
     * </pre>
     */
    @Bean
    @SuppressWarnings("deprecation")
    public UserDetailsService userDetailsService() {
        var tenantUser = User.withDefaultPasswordEncoder()
                .username("tenant-user")
                .password("secret")
                .roles(UserRole.TENANT_USER.name())
                .build();

        var globalAdmin = User.withDefaultPasswordEncoder()
                .username("global-admin")
                .password("secret")
                .roles(UserRole.GLOBAL_ADMIN.name())
                .build();

        return new InMemoryUserDetailsManager(tenantUser, globalAdmin);
    }
}