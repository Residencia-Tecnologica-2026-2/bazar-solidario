package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AuthResponse;
import com.isac.bazar_solidario.dto.LoginRequest;
import com.isac.bazar_solidario.dto.RegistroRequest;
import com.isac.bazar_solidario.dto.UsuarioResponse;
import com.isac.bazar_solidario.model.Loja;
import com.isac.bazar_solidario.model.Role;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.repository.LojaRepository;
import com.isac.bazar_solidario.repository.UsuarioRepository;
import com.isac.bazar_solidario.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final LojaRepository lojaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          LojaRepository lojaRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.lojaRepository = lojaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse cadastrar(RegistroRequest req) {
        String email = req.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Role role = req.tipo() != null ? req.tipo() : Role.CLIENTE;
        if (role != Role.CLIENTE && role != Role.LOJA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de conta inválido");
        }
        if (role == Role.LOJA && (req.whatsapp() == null || !req.whatsapp().matches("^[0-9]{10,13}$"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o WhatsApp da loja com DDI e DDD");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario.setRole(role);
        usuario = usuarioRepository.save(usuario);

        if (role == Role.LOJA) {
            String whatsapp = req.whatsapp();
            Loja loja = new Loja();
            loja.setNome(req.nome().trim());
            loja.setWhatsapp(whatsapp);
            loja.setUsuario(usuario);
            lojaRepository.save(loja);
        }

        return montarResposta(usuario);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

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
