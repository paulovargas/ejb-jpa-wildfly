package br.com.exemplo.rest;

import br.com.exemplo.dto.ErroResponse;
import br.com.exemplo.dto.LoginRequest;
import br.com.exemplo.dto.LoginResponse;
import br.com.exemplo.dto.UsuarioResponse;
import br.com.exemplo.security.LoginRateLimiter;
import br.com.exemplo.service.AuthService;
import br.com.exemplo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
@Tag(name = "Autenticação", description = "Login e consulta da identidade autenticada")
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
  @Operation(operationId = "login", summary = "Autenticar usuário",
      description = "Retorna um JWT. Copie accessToken e informe em Authorize. Limite de 10 chamadas por minuto por endereço remoto.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Token emitido", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
      @ApiResponse(responseCode = "400", description = "Email ou campos inválidos", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "401", description = "Email ou senha inválidos", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "429", description = "Limite de tentativas excedido; aguarde o Retry-After", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
  })
  public Response login(@NotNull @Valid LoginRequest request, @Parameter(hidden = true) @Context HttpServletRequest servletRequest) {
    limitador.verificar(servletRequest.getRemoteAddr());
    return Response.ok(auth.login(request.getEmail(), request.getSenha()))
        .header("Cache-Control", "no-store").header("Pragma", "no-cache").build();
  }

  @GET
  @Path("/me")
  @SecurityRequirement(name = "bearerAuth")
  @Operation(operationId = "usuarioAtual", summary = "Consultar usuário autenticado", description = "Disponível para ADMIN e OPERADOR.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Identidade e perfis atuais", content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
      @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "403", description = "Acesso sem permissão")
  })
  public UsuarioResponse atual() { return usuarios.atual(); }
}
