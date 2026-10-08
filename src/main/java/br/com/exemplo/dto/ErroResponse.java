package br.com.exemplo.dto;

public class ErroResponse {
  private final String mensagem;

  public ErroResponse(String mensagem) { this.mensagem = mensagem; }
  public String getMensagem() { return mensagem; }
}
