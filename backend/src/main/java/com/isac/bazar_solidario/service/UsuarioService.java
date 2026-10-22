package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AuthResponse;
import com.isac.bazar_solidario.dto.LoginRequest;
import com.isac.bazar_solidario.dto.RegistroRequest;
import com.isac.bazar_solidario.dto.UsuarioResponse;
import com.isac.bazar_solidario.model.Role;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.repository.UsuarioRepository;
import com.isac.bazar_solidario.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Cadastra o usuário (senha criptografada) e já devolve um token. */
    public AuthResponse cadastrar(RegistroRequest req) {
        String email = req.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario.setRole(Role.CLIENTE); // sempre CLIENTE, ninguém se cadastra como ADMIN
        usuario = usuarioRepository.save(usuario);

        return montarResposta(usuario);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        // mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem)
        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenha()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

        return montarResposta(usuario);
    }

    private AuthResponse montarResposta(Usuario usuario) {
        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getRole().name());
        return AuthResponse.de(token, jwtService.getExpiracaoMs(), UsuarioResponse.de(usuario));
    }
}
