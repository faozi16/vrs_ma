package com.af.vrs.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomerUserDetailsService customerUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomerUserDetailsService customerUserDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customerUserDetailsService = customerUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customerUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/",
                                 "/error",
                                 "/favicon.ico",
                                 "/actuator/health",
                                 "/actuator/prometheus",
                                 "/api/auth/**",
                                 "/api/customers/create", 
                                 "/v3/api-docs/**", 
                                 "/swagger-ui/**", 
                                 "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/customers/get").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/drivers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/drivers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/drivers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/drivers/**").hasAnyRole("ADMIN", "DRIVER")

                .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("ADMIN", "DRIVER", "CUSTOMER")

                .requestMatchers(HttpMethod.GET, "/api/reservations/get").hasAnyRole("ADMIN", "DRIVER")
                .requestMatchers(HttpMethod.POST, "/api/reservations/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/reservations/**").hasAnyRole("ADMIN", "CUSTOMER", "DRIVER")
                .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/reservations/**").hasAnyRole("ADMIN", "CUSTOMER", "DRIVER")

                .requestMatchers(HttpMethod.GET, "/api/payments/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/payments/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/payments/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/api/payments/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/payment-methods/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/payment-methods/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/payment-methods/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/api/payment-methods/**").hasAnyRole("ADMIN", "CUSTOMER")

                .requestMatchers(HttpMethod.GET, "/api/feedbacks/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/feedbacks/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/feedbacks/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/api/feedbacks/**").hasAnyRole("ADMIN", "CUSTOMER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
