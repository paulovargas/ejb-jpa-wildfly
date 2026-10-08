package br.com.exemplo.service;

import br.com.exemplo.dto.LoginResponse;
import br.com.exemplo.entity.Perfil;
import br.com.exemplo.entity.Usuario;
import br.com.exemplo.exception.ApiException;
import br.com.exemplo.repository.UsuarioRepository;
import br.com.exemplo.security.JwtService;
import br.com.exemplo.security.SenhaService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
  @Mock UsuarioRepository usuarios;
  @Mock SenhaService senhas;
  @Mock JwtService jwt;
  @InjectMocks AuthService auth;
  private AutoCloseable mocks;
  private Usuario usuario;

  @BeforeEach void preparar() {
    mocks = MockitoAnnotations.openMocks(this);
    usuario = new Usuario();
    usuario.setId(1L);
    usuario.setSenhaHash("hash");
    usuario.setPerfis(Collections.singleton(Perfil.ADMIN));
    when(usuarios.buscarPorEmail("admin@exemplo.com")).thenReturn(Optional.of(usuario));
  }
  @AfterEach void fechar() throws Exception { mocks.close(); }

  @Test void deveRetornarTokenParaCredenciaisValidas() {
    when(senhas.verificar("senha", "hash")).thenReturn(true);
    when(jwt.emitir(usuario)).thenReturn("token");
    when(jwt.getExpiresIn()).thenReturn(900L);
    LoginResponse response = auth.login("admin@exemplo.com", "senha");
    assertEquals("token", response.getAccessToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(900, response.getExpiresIn());
  }

  @Test void deveRejeitarSenhaIncorretaSemEmitirToken() {
    ApiException erro = assertThrows(ApiException.class, () -> auth.login("admin@exemplo.com", "errada"));
    assertEquals(401, erro.getStatus());
    verifyNoInteractions(jwt);
  }

  @Test void deveCalcularHashMesmoParaUsuarioInexistente() {
    when(usuarios.buscarPorEmail("outro@exemplo.com")).thenReturn(Optional.empty());
    ApiException erro = assertThrows(ApiException.class, () -> auth.login("outro@exemplo.com", "senha"));
    assertEquals("Email ou senha inválidos.", erro.getMessage());
    verify(senhas).verificar("senha", null);
    verifyNoInteractions(jwt);
  }

  @Test void deveRejeitarUsuarioInativoMesmoComSenhaCorreta() {
    usuario.setAtivo(false);
    when(senhas.verificar("senha", "hash")).thenReturn(true);
    assertThrows(ApiException.class, () -> auth.login("admin@exemplo.com", "senha"));
    verifyNoInteractions(jwt);
  }
}
