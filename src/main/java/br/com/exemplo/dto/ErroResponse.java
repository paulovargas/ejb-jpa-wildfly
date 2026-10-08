package br.com.exemplo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ErroResponse {
  @Schema(description = "Descrição do erro", example = "Email ou senha inválidos.")
  private final String mensagem;

  public ErroResponse(String mensagem) { this.mensagem = mensagem; }
  public String getMensagem() { return mensagem; }
}
