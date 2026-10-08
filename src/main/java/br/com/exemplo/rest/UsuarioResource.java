package br.com.exemplo.rest;

import br.com.exemplo.dto.CriarUsuarioRequest;
import br.com.exemplo.dto.ErroResponse;
import br.com.exemplo.dto.UsuarioResponse;
import br.com.exemplo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/usuarios")
@Tag(name = "Usuários", description = "Administração de usuários e perfis")
@SecurityRequirement(name = "bearerAuth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {
  @Inject
  private UsuarioService usuarios;

  @POST
  @Operation(operationId = "criarUsuario", summary = "Cadastrar usuário",
      description = "Exclusivo de ADMIN. A senha deve ter de 12 a 128 caracteres. O perfil padrão é OPERADOR.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Usuário criado", content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "403", description = "Apenas administradores podem criar usuários"),
      @ApiResponse(responseCode = "409", description = "Email já cadastrado", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
  })
  public Response criar(@NotNull @Valid CriarUsuarioRequest request) {
    UsuarioResponse usuario = usuarios.criar(request);
    return Response.status(Response.Status.CREATED).entity(usuario).build();
  }
}
