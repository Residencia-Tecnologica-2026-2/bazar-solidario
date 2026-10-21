package com.isac.bazar_solidario.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Segurança só para os testes de controller. Simula as regras que o módulo de anúncios
 * precisa do SecurityConfig real: GETs públicos liberados, o resto autenticado.
 * (O SecurityConfig de produção não foi alterado.)
 */
@TestConfiguration
class TestSecurityConfig {

    @Bean
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/anuncios/meus").authenticated()
                        .requestMatchers(HttpMethod.GET, "/anuncios", "/anuncios/*", "/categorias").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}

