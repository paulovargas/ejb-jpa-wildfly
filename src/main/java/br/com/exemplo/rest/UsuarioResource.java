package br.com.exemplo.rest;

import br.com.exemplo.dto.CriarUsuarioRequest;
import br.com.exemplo.dto.UsuarioResponse;
import br.com.exemplo.service.UsuarioService;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {
  @Inject
  private UsuarioService usuarios;

  @POST
  public Response criar(@NotNull @Valid CriarUsuarioRequest request) {
    UsuarioResponse usuario = usuarios.criar(request);
    return Response.status(Response.Status.CREATED).entity(usuario).build();
  }
}
