package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Único ponto do módulo que depende de COMO o login funciona.
 * Premissa atual: authentication.getName() devolve o e-mail do usuário.
 * Quando o time definir a autenticação (JWT etc.), só esta classe precisa mudar.
 */
@Service
@RequiredArgsConstructor
public class UsuarioAutenticadoService {

    private final VendedorRepository usuarioRepository;

    public Usuario obter(Authentication authentication) {
        return obterOpcional(authentication)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
    }

    /** Para rotas públicas: vazio se for visitante anônimo. */
    public Optional<Usuario> obterOpcional(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmail(authentication.getName());
    }
}

