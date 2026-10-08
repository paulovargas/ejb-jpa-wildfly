package br.com.exemplo.rest;

import br.com.exemplo.dto.ErroResponse;
import br.com.exemplo.entity.Cliente;
import br.com.exemplo.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/clientes")
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes")
@SecurityRequirement(name = "bearerAuth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {
  @Inject
  private ClienteService service;

  @POST
  @Operation(operationId = "cadastrarCliente", summary = "Cadastrar cliente", description = "Disponível para ADMIN e OPERADOR. Omita o ID, gerado pelo banco.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Cliente cadastrado, sem corpo de resposta"),
      @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "403", description = "Acesso sem permissão")
  })
  public void salvar(Cliente c) { service.salvar(c); }

  @GET
  @Operation(operationId = "listarClientes", summary = "Listar clientes", description = "Disponível para ADMIN e OPERADOR. Retorna uma lista vazia quando não há clientes.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Clientes cadastrados", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cliente.class)))),
      @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "403", description = "Acesso sem permissão")
  })
  public List<Cliente> listar() { return service.listar(); }
}
