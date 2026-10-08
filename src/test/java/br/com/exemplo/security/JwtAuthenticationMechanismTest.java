package br.com.exemplo.security;

import br.com.exemplo.entity.Perfil;
import br.com.exemplo.entity.Usuario;
import br.com.exemplo.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationMechanismTest {
  @Mock JwtService jwt;
  @Mock UsuarioRepository usuarios;
  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Mock HttpMessageContext contexto;
  @InjectMocks JwtAuthenticationMechanism mecanismo;
  private AutoCloseable mocks;
  private StringWriter corpo;

  @BeforeEach void preparar() throws Exception {
    mocks = MockitoAnnotations.openMocks(this);
    corpo = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(corpo));
    when(request.getContextPath()).thenReturn("/app");
    when(request.getRequestURI()).thenReturn("/app/api/clientes");
    when(contexto.isProtected()).thenReturn(true);
    when(request.getHeaders("Authorization")).thenReturn(Collections.emptyEnumeration());
  }
  @AfterEach void fechar() throws Exception { mocks.close(); }

  private void header(String valor) {
    when(request.getHeaders("Authorization")).thenReturn(Collections.enumeration(Collections.singleton(valor)));
  }

  @Test void deveLiberarLoginSemToken() {
    when(request.getRequestURI()).thenReturn("/app/api/auth/login");
    when(contexto.doNothing()).thenReturn(AuthenticationStatus.NOT_DONE);
    assertEquals(AuthenticationStatus.NOT_DONE, mecanismo.validateRequest(request, response, contexto));
    verifyNoInteractions(jwt, usuarios);
  }

  @Test void deveNegarTokenAusente() {
    assertEquals(AuthenticationStatus.SEND_FAILURE, mecanismo.validateRequest(request, response, contexto));
    verify(response).setStatus(401);
    assertTrue(corpo.toString().contains("Token ausente"));
    verifyNoInteractions(jwt, usuarios);
  }

  @Test void deveNegarHeadersDuplicadosOuFormatoInvalido() {
    header("Basic abc");
    assertEquals(AuthenticationStatus.SEND_FAILURE, mecanismo.validateRequest(request, response, contexto));
    when(request.getHeaders("Authorization")).thenReturn(Collections.enumeration(Arrays.asList("Bearer a.b.c", "Bearer a.b.c")));
    assertEquals(AuthenticationStatus.SEND_FAILURE, mecanismo.validateRequest(request, response, contexto));
    verifyNoInteractions(jwt, usuarios);
  }

  @Test void deveNegarTokenInvalido() {
    header("Bearer a.b.c");
    when(jwt.validar("a.b.c")).thenThrow(new JwtException("inválido"));
    assertEquals(AuthenticationStatus.SEND_FAILURE, mecanismo.validateRequest(request, response, contexto));
    verifyNoInteractions(usuarios);
  }

  @Test void deveNegarUsuarioInativoOuRemovido() {
    header("Bearer a.b.c");
    when(jwt.validar("a.b.c")).thenReturn(7L);
    when(usuarios.buscarPorId(7L)).thenReturn(Optional.empty());
    assertEquals(AuthenticationStatus.SEND_FAILURE, mecanismo.validateRequest(request, response, contexto));
    Usuario usuario = new Usuario();
    usuario.setAtivo(false);
    when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(usuario));
    assertEquals(AuthenticationStatus.SEND_FAILURE, mecanismo.validateRequest(request, response, contexto));
  }

  @Test void deveNotificarContainerComIdentidadeEPerfisAtuaisDoBanco() {
    header("bearer a.b.c");
    Usuario usuario = new Usuario();
    usuario.setId(7L);
    usuario.setPerfis(Collections.singleton(Perfil.OPERADOR));
    when(jwt.validar("a.b.c")).thenReturn(7L);
    when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(usuario));
    when(contexto.notifyContainerAboutLogin("7", Collections.singleton("OPERADOR"))).thenReturn(AuthenticationStatus.SUCCESS);
    assertEquals(AuthenticationStatus.SUCCESS, mecanismo.validateRequest(request, response, contexto));
    verify(contexto).notifyContainerAboutLogin("7", Collections.singleton("OPERADOR"));
  }
}
