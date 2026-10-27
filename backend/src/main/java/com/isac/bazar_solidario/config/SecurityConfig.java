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
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios", "/auth/login").permitAll()
                        .requestMatchers("/anuncios/meus").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/anuncios/*/vendido").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/lojas/me").hasRole("LOJA")
                        .requestMatchers(HttpMethod.GET, "/lojas/me").hasRole("LOJA")
                        .requestMatchers(HttpMethod.GET, "/encontros", "/encontros/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/encontros").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PUT, "/encontros/*").hasRole("LOJA")
                        .requestMatchers(HttpMethod.PATCH, "/encontros/*/cancelar").hasRole("LOJA")
                        .requestMatchers(HttpMethod.GET, "/anuncios", "/anuncios/*", "/categorias", "/lojas", "/lojas/*").permitAll()
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
