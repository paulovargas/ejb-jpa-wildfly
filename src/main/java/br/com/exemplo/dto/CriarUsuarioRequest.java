package br.com.exemplo.dto;

import br.com.exemplo.entity.Perfil;
import javax.validation.constraints.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CriarUsuarioRequest {
  @NotBlank @Size(max = 100)
  private String nome;
  @NotBlank @Email @Size(max = 254)
  private String email;
  @NotBlank @Size(min = 12, max = 128)
  private String senha;
  @NotEmpty
  private Set<@NotNull Perfil> perfis = new HashSet<>(Collections.singleton(Perfil.OPERADOR));

  public String getNome() { return nome; }
  public void setNome(String nome) { this.nome = nome; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getSenha() { return senha; }
  public void setSenha(String senha) { this.senha = senha; }
  public Set<Perfil> getPerfis() { return perfis; }
  public void setPerfis(Set<Perfil> perfis) { this.perfis = perfis; }
}
