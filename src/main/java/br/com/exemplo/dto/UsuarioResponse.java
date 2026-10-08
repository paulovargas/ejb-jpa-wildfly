package br.com.exemplo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.exemplo.entity.Perfil;
import br.com.exemplo.entity.Usuario;
import java.util.HashSet;
import java.util.Set;

public class UsuarioResponse {
  @Schema(example = "1")
  private final Long id;
  @Schema(example = "Administrador")
  private final String nome;
  @Schema(example = "admin@exemplo.com", format = "email")
  private final String email;
  private final Set<Perfil> perfis;

  public UsuarioResponse(Usuario usuario) {
    id = usuario.getId();
    nome = usuario.getNome();
    email = usuario.getEmail();
    perfis = new HashSet<>(usuario.getPerfis());
  }

  public Long getId() { return id; }
  public String getNome() { return nome; }
  public String getEmail() { return email; }
  public Set<Perfil> getPerfis() { return perfis; }
}
