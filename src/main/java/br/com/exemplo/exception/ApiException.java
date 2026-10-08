package br.com.exemplo.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ApiException extends RuntimeException {
  private final int status;

  public ApiException(int status, String mensagem) {
    super(mensagem);
    this.status = status;
  }

  public int getStatus() { return status; }
}
