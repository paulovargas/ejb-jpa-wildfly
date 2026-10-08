package br.com.exemplo.service;

import br.com.exemplo.dto.CriarUsuarioRequest;
import br.com.exemplo.dto.UsuarioResponse;
import br.com.exemplo.entity.Usuario;
import br.com.exemplo.exception.ApiException;
import br.com.exemplo.repository.UsuarioRepository;
import br.com.exemplo.security.SenhaService;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Stateless
@RolesAllowed("ADMIN")
public class UsuarioService {
  @Inject
  private UsuarioRepository usuarios;
  @Inject
  private SenhaService senhas;
  @Resource
  private SessionContext contexto;

  public UsuarioResponse criar(@NotNull @Valid CriarUsuarioRequest request) {
    if (usuarios.buscarPorEmail(request.getEmail()).isPresent()) {
      throw new ApiException(409, "Já existe um usuário com esse email.");
    }
    Usuario usuario = new Usuario();
    usuario.setNome(request.getNome().trim());
    usuario.setEmail(request.getEmail());
    usuario.setSenhaHash(senhas.gerar(request.getSenha()));
    usuario.setPerfis(request.getPerfis());
    usuarios.salvar(usuario);
    return new UsuarioResponse(usuario);
  }

  @RolesAllowed({"ADMIN", "OPERADOR"})
  public UsuarioResponse atual() {
    // A identidade vem do container e também poderá ser usada pela auditoria.
    Long id = Long.valueOf(contexto.getCallerPrincipal().getName());
    return new UsuarioResponse(usuarios.buscarPorId(id)
        .orElseThrow(() -> new ApiException(401, "Usuário não está disponível.")));
  }
}
