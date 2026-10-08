package br.com.exemplo.rest;

import br.com.exemplo.dto.ErroResponse;
import br.com.exemplo.exception.ApiException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
  @Override
  public Response toResponse(ApiException exception) {
    Response.ResponseBuilder response = Response.status(exception.getStatus())
        .type(MediaType.APPLICATION_JSON).entity(new ErroResponse(exception.getMessage()))
        .header("Cache-Control", "no-store");
    if (exception.getStatus() == 401) {
      response.header("WWW-Authenticate", "Bearer realm=\"ejb-jpa-wildfly\"");
    }
    if (exception.getStatus() == 429) { response.header("Retry-After", "60"); }
    return response.build();
  }
}
