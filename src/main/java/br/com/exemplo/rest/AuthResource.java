package br.com.exemplo.rest;

import br.com.exemplo.dto.LoginRequest;
import br.com.exemplo.dto.UsuarioResponse;
import br.com.exemplo.security.LoginRateLimiter;
import br.com.exemplo.service.AuthService;
import br.com.exemplo.service.UsuarioService;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
  @Inject
  private AuthService auth;
  @Inject
  private UsuarioService usuarios;
  @Inject
  private LoginRateLimiter limitador;

  @POST
  @Path("/login")
  public Response login(@NotNull @Valid LoginRequest request, @Context HttpServletRequest servletRequest) {
    limitador.verificar(servletRequest.getRemoteAddr());
    return Response.ok(auth.login(request.getEmail(), request.getSenha()))
        .header("Cache-Control", "no-store").header("Pragma", "no-cache").build();
  }

  @GET
  @Path("/me")
  public UsuarioResponse atual() { return usuarios.atual(); }
}
