package br.com.exemplo.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginRequest {
  @NotBlank @Email @Size(max = 254)
  private String email;
  @NotBlank @Size(max = 128)
  private String senha;

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getSenha() { return senha; }
  public void setSenha(String senha) { this.senha = senha; }
}
