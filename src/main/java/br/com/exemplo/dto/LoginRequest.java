package br.com.exemplo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginRequest {
  @NotBlank @Email @Size(max = 254)
  @Schema(description = "Email cadastrado", example = "admin@exemplo.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;
  @NotBlank @Size(max = 128)
  @Schema(description = "Senha do usuário", example = "SenhaExemplo!123", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
  private String senha;

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getSenha() { return senha; }
  public void setSenha(String senha) { this.senha = senha; }
}
