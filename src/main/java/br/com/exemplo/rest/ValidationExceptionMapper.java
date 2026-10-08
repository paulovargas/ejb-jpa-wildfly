package br.com.exemplo.rest;

import br.com.exemplo.dto.ErroResponse;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
  @Override
  public Response toResponse(ConstraintViolationException exception) {
    // Não serializar violações: elas podem incluir a senha recebida.
    return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
        .entity(new ErroResponse("Dados inválidos. Verifique os campos obrigatórios e seus formatos."))
        .build();
  }
}
