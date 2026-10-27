package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AuthResponse;
import com.isac.bazar_solidario.dto.LoginRequest;
import com.isac.bazar_solidario.dto.RegistroRequest;
import com.isac.bazar_solidario.model.Role;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.repository.LojaRepository;
import com.isac.bazar_solidario.repository.UsuarioRepository;
import com.isac.bazar_solidario.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository repo;
    private LojaRepository lojaRepo;
    private JwtService jwt;
    private BCryptPasswordEncoder encoder;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repo = mock(UsuarioRepository.class);
        lojaRepo = mock(LojaRepository.class);
        encoder = new BCryptPasswordEncoder();
        jwt = new JwtService("uma-chave-de-teste-com-mais-de-32-caracteres!!", 60_000);
        service = new UsuarioService(repo, lojaRepo, encoder, jwt);
        when(repo.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    @Test
    void cadastroCriptografaSenhaForcaClienteEDevolveToken() {
        AuthResponse r = service.cadastrar(new RegistroRequest("Ana", "ANA@x.com", "123456", null, null));

        assertEquals("ana@x.com", r.usuario().email());
        assertEquals("CLIENTE", r.usuario().role());
        assertEquals("ana@x.com", jwt.validar(r.token()).orElseThrow().getSubject());

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertTrue(encoder.matches("123456", captor.getValue().getSenha()));
    }

    @Test
    void cadastroEmailDuplicadoLancaConflict() {
        when(repo.existsByEmail("ana@x.com")).thenReturn(true);
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrar(new RegistroRequest("Ana", "ana@x.com", "123456", null, null)));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void loginSenhaErradaLancaUnauthorized() {
        Usuario u = new Usuario();
        u.setEmail("ana@x.com");
        u.setSenha(encoder.encode("correta"));
        u.setRole(Role.CLIENTE);
        when(repo.findByEmail("ana@x.com")).thenReturn(Optional.of(u));

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("ana@x.com", "errada")));
        assertEquals(401, ex.getStatusCode().value());
    }
}
