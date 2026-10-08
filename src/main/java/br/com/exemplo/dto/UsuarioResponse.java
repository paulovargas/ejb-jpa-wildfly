package br.com.exemplo.dto;

import br.com.exemplo.entity.Perfil;
import br.com.exemplo.entity.Usuario;
import java.util.HashSet;
import java.util.Set;

public class UsuarioResponse {
  private final Long id;
  private final String nome;
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
