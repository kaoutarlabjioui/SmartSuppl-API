package org.smartsupply.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security. config.annotation.method.configuration. EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config. annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security. web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer:: disable)




                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().none()
                        .enableSessionUrlRewriting(false)
                        .maximumSessions(-1)
                )


                .securityContext(context -> context
                        .requireExplicitSave(true)
                        .securityContextRepository(new NullSecurityContextRepository())
                )


                . requestCache(cache -> cache
                        .requestCache(new NullRequestCache())
                )




                .authorizeHttpRequests(auth -> auth
                        // === ACCÈS PUBLIC ===
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // === CATALOGUE PRODUITS PUBLIC ===
                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/category/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()


                        // === UTILISATEURS ===
                        // Profil personnel (R/U) - Tous les rôles authentifiés
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()

                        // Gestion des utilisateurs (CRUD) - ADMIN uniquement
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // === PRODUITS (Gestion) ===
                        // CREATE, UPDATE, DELETE - ADMIN uniquement
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // === ENTREPÔTS (WAREHOUSES) ===
                        . requestMatchers(HttpMethod.POST, "/api/warehouses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/warehouses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/warehouses/**").hasRole("ADMIN")
                        . requestMatchers(HttpMethod.GET, "/api/warehouses/**").authenticated()

                        // === INVENTORY ===
                        .requestMatchers("/api/inventory/**").hasRole("ADMIN")

                        // === MOUVEMENTS STOCK ===
                        .requestMatchers(HttpMethod.POST, "/api/stock-movements/**").hasRole("WAREHOUSE_MANAGER")
                        . requestMatchers(HttpMethod.PUT, "/api/stock-movements/**").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/stock-movements/**").hasRole("WAREHOUSE_MANAGER")
                        . requestMatchers(HttpMethod.GET, "/api/stock-movements/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // === FOURNISSEURS (SUPPLIERS) ===
                        .requestMatchers(HttpMethod.POST, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // === PURCHASE ORDERS ===
                        . requestMatchers(HttpMethod.POST, "/api/purchase-orders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/purchase-orders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // === COMMANDES (SALES ORDERS) ===
                        .requestMatchers(HttpMethod.POST, "/api/sales-orders").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/sales-orders/*/status").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/sales-orders/*/ship").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/sales-orders/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/sales-orders/**").authenticated()

                        // === SHIPMENTS ===
                        .requestMatchers(HttpMethod.POST, "/api/shipments/**").hasRole("WAREHOUSE_MANAGER")
                        . requestMatchers(HttpMethod.PUT, "/api/shipments/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
                        . requestMatchers(HttpMethod.PATCH, "/api/shipments/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shipments/**").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/**").authenticated()

                        // Toutes les autres requêtes API nécessitent une authentification
                        .requestMatchers("/api/**").authenticated()
                )


                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\"," +
                                            "\"message\":\"" + authException.getMessage() + "\"," +
                                            "\"path\": \"" + request.getRequestURI() + "\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"Forbidden\"," +
                                            "\"message\": \"" + accessDeniedException. getMessage() + "\"," +
                                            "\"path\":\"" + request.getRequestURI() + "\"}"
                            );
                        })
                )


                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}