package br.com.exemplo.service;

import br.com.exemplo.dto.LoginResponse;
import br.com.exemplo.entity.Usuario;
import br.com.exemplo.exception.ApiException;
import br.com.exemplo.repository.UsuarioRepository;
import br.com.exemplo.security.JwtService;
import br.com.exemplo.security.SenhaService;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
@PermitAll
public class AuthService {
  @Inject
  private UsuarioRepository usuarios;
  @Inject
  private SenhaService senhas;
  @Inject
  private JwtService jwt;

  public LoginResponse login(String email, String senha) {
    Usuario usuario = usuarios.buscarPorEmail(email).orElse(null);
    boolean senhaValida = senhas.verificar(senha, usuario == null ? null : usuario.getSenhaHash());
    if (!senhaValida || usuario == null || !usuario.isAtivo() || usuario.getPerfis().isEmpty()) {
      throw new ApiException(401, "Email ou senha inválidos.");
    }
    return new LoginResponse(jwt.emitir(usuario), jwt.getExpiresIn());
  }
}
