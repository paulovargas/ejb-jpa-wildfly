package br.com.exemplo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.exemplo.entity.Perfil;
import javax.validation.constraints.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CriarUsuarioRequest {
  @NotBlank @Size(max = 100)
  @Schema(example = "Operador", requiredMode = Schema.RequiredMode.REQUIRED)
  private String nome;
  @NotBlank @Email @Size(max = 254)
  @Schema(example = "operador@exemplo.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;
  @NotBlank @Size(min = 12, max = 128)
  @Schema(example = "SenhaExemplo!123", format = "password", minLength = 12, maxLength = 128, accessMode = Schema.AccessMode.WRITE_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
  private String senha;
  @NotEmpty
  @Schema(description = "Perfis de acesso. Se omitido, utiliza OPERADOR.", example = "[\"OPERADOR\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
