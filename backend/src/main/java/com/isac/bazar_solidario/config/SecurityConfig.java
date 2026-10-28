package com.isac.bazar_solidario.config;

import com.isac.bazar_solidario.security.JwtAuthFilter;
import com.isac.bazar_solidario.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // cadastro e login são públicos (é assim que se consegue o token)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/uploads/**", "/home").permitAll()
                        .requestMatchers(HttpMethod.POST, "/uploads").authenticated()
                        // ── anúncios: leitura pública, gestão só para LOJA ──
                        .requestMatchers(HttpMethod.GET, "/anuncios/meus").authenticated()
                        .requestMatchers(HttpMethod.GET, "/lojas/me").hasRole("LOJA")
                        .requestMatchers(HttpMethod.GET, "/anuncios", "/anuncios/*", "/categorias", "/lojas", "/lojas/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/anuncios").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PUT, "/anuncios/*").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PATCH, "/anuncios/*/vendido").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PATCH, "/anuncios/*/desativar").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PATCH, "/anuncios/*/ativar").hasRole("LOJA")
                        .requestMatchers(HttpMethod.DELETE, "/anuncios/*").hasRole("LOJA")
                        // ── lojas: gestão só para LOJA ──
                        .requestMatchers(HttpMethod.PUT, "/lojas/me").hasRole("LOJA")
                        .requestMatchers(HttpMethod.GET, "/lojas/me").hasRole("LOJA")
                        // ── encontros ──
                        .requestMatchers(HttpMethod.GET, "/encontros", "/encontros/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/encontros").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PUT, "/encontros/*").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PATCH, "/encontros/*/cancelar").hasRole("LOJA")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setCharacterEncoding("UTF-8");
                    res.getWriter().write("{\"status\":401,\"erro\":\"Não autenticado: envie o token no cabeçalho Authorization: Bearer <token>\"}");
                }))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
