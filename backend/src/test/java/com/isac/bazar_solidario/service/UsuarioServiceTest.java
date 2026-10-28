package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AuthResponse;
import com.isac.bazar_solidario.dto.LoginRequest;
import com.isac.bazar_solidario.dto.RegistroRequest;
import com.isac.bazar_solidario.model.Role;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.repository.UsuarioRepository;
import com.isac.bazar_solidario.repository.LojaRepository;
import com.isac.bazar_solidario.model.Loja;
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
    private JwtService jwt;
    private BCryptPasswordEncoder encoder;
    private UsuarioService service;
    private LojaRepository lojaRepo;

    @BeforeEach
    void setUp() {
        repo = mock(UsuarioRepository.class);
        encoder = new BCryptPasswordEncoder();
        jwt = new JwtService("uma-chave-de-teste-com-mais-de-32-caracteres!!", 60_000);
        lojaRepo = mock(LojaRepository.class);
        service = new UsuarioService(repo, encoder, jwt, lojaRepo);
        when(repo.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    @Test
    void cadastroCriptografaSenhaCriaLojaComRoleLojaEDevolveToken() {
        AuthResponse r = service.cadastrar(new RegistroRequest("Ana", "ANA@x.com", "123456", "5581999999999"));

        assertEquals("ana@x.com", r.usuario().email());
        assertEquals("LOJA", r.usuario().role());
        assertEquals("ana@x.com", jwt.validar(r.token()).orElseThrow().getSubject());

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertNotEquals("123456", captor.getValue().getSenha());
        assertTrue(encoder.matches("123456", captor.getValue().getSenha()));
        assertEquals(Role.LOJA, captor.getValue().getRole());
        var lojaCaptor = org.mockito.ArgumentCaptor.forClass(Loja.class);
        verify(lojaRepo).save(lojaCaptor.capture());
        assertEquals("Ana", lojaCaptor.getValue().getNome());
        assertEquals("5581999999999", lojaCaptor.getValue().getWhatsapp());
    }

    @Test
    void emailRepetidoRetorna409() {
        when(repo.existsByEmail("ana@x.com")).thenReturn(true);
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrar(new RegistroRequest("Ana", "ana@x.com", "123456", "5581999999999")));
        assertEquals(409, ex.getStatusCode().value());
        verify(repo, never()).save(any());
        verify(lojaRepo, never()).save(any(Loja.class));
    }

    @Test
    void loginCorretoDevolveToken() {
        Usuario u = new Usuario(1L, "Ana", "ana@x.com", encoder.encode("123456"), Role.LOJA);
        when(repo.findByEmail("ana@x.com")).thenReturn(Optional.of(u));

        AuthResponse r = service.login(new LoginRequest("ana@x.com", "123456"));
        assertTrue(jwt.validar(r.token()).isPresent());
    }

    @Test
    void loginNormalizaEmailAntesDeBuscarUsuario() {
        Usuario u = new Usuario(1L, "Ana", "ana@x.com", encoder.encode("123456"), Role.LOJA);
        when(repo.findByEmail("ana@x.com")).thenReturn(Optional.of(u));

        AuthResponse r = service.login(new LoginRequest("  ANA@x.com  ", "123456"));

        assertEquals("ana@x.com", jwt.validar(r.token()).orElseThrow().getSubject());
        verify(repo).findByEmail("ana@x.com");
    }

    @Test
    void senhaErradaOuEmailInexistenteRetorna401() {
        Usuario u = new Usuario(1L, "Ana", "ana@x.com", encoder.encode("123456"), Role.LOJA);
        when(repo.findByEmail("ana@x.com")).thenReturn(Optional.of(u));
        when(repo.findByEmail("nao@x.com")).thenReturn(Optional.empty());

        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("ana@x.com", "errada"))).getStatusCode().value());
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("nao@x.com", "123456"))).getStatusCode().value());
    }

    @Test
    void tokenAdulteradoOuExpiradoEInvalido() {
        String token = jwt.gerarToken("ana@x.com", "LOJA");
        assertTrue(jwt.validar(token + "x").isEmpty());
        assertTrue(jwt.validar("lixo").isEmpty());

        JwtService expirado = new JwtService("uma-chave-de-teste-com-mais-de-32-caracteres!!", -1000);
        assertTrue(jwt.validar(expirado.gerarToken("ana@x.com", "LOJA")).isEmpty());
    }
}
