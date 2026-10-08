package br.com.exemplo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponse {
  @Schema(description = "JWT assinado para enviar no header Authorization: Bearer <token>")
  private final String accessToken;
  @Schema(description = "Validade do token em segundos", example = "900")
  private final long expiresIn;

  public LoginResponse(String accessToken, long expiresIn) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
  }

  public String getAccessToken() { return accessToken; }
  @Schema(example = "Bearer", allowableValues = {"Bearer"})
  public String getTokenType() { return "Bearer"; }
  public long getExpiresIn() { return expiresIn; }
}
