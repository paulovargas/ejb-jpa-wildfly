
package br.com.exemplo.rest;

import br.com.exemplo.entity.Cliente;
import br.com.exemplo.service.ClienteService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

  @Inject
  private ClienteService service;

  @POST
  public void salvar(Cliente c) {
    service.salvar(c);
  }

  @GET
  public List<Cliente> listar() {
    return service.listar();
  }
}
