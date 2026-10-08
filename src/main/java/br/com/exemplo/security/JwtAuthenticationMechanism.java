package br.com.exemplo.security;

import br.com.exemplo.entity.Usuario;
import br.com.exemplo.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class JwtAuthenticationMechanism implements HttpAuthenticationMechanism {
  @Inject
  private JwtService jwt;
  @Inject
  private UsuarioRepository usuarios;

  @Override
  public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response,
                                               HttpMessageContext contexto) {
    String caminho = request.getRequestURI().substring(request.getContextPath().length());
    if ("/api/auth/login".equals(caminho)) { return contexto.doNothing(); }
    if (!contexto.isProtected() && !caminho.startsWith("/api/")) { return contexto.doNothing(); }
    List<String> headers = Collections.list(request.getHeaders("Authorization"));
    if (headers.size() != 1) { return negar(response, contexto); }
    String header = headers.get(0);
    if (header.length() > 4096 || !header.matches("(?i)^Bearer [A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")) {
      return negar(response, contexto);
    }
    Long id;
    try {
      id = jwt.validar(header.substring(7));
    } catch (JwtException | IllegalArgumentException e) {
      return negar(response, contexto);
    }
    Usuario usuario = usuarios.buscarPorId(id).orElse(null);
    if (usuario == null || !usuario.isAtivo() || usuario.getPerfis().isEmpty()) {
      return negar(response, contexto);
    }
    // Os perfis atuais do banco prevalecem sobre os presentes no token.
    Set<String> perfis = usuario.getPerfis().stream().map(Enum::name).collect(Collectors.toSet());
    return contexto.notifyContainerAboutLogin(usuario.getId().toString(), perfis);
  }

  private AuthenticationStatus negar(HttpServletResponse response, HttpMessageContext contexto) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setHeader("WWW-Authenticate", "Bearer realm=\"ejb-jpa-wildfly\"");
    response.setHeader("Cache-Control", "no-store");
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try {
      response.getWriter().write("{\"mensagem\":\"Token ausente, inválido ou expirado.\"}");
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível responder à autenticação.", e);
    }
    return AuthenticationStatus.SEND_FAILURE;
  }
}
