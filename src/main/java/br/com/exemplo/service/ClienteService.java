
package br.com.exemplo.service;

import br.com.exemplo.entity.Cliente;
import javax.ejb.Stateless;
import javax.annotation.security.RolesAllowed;
import javax.persistence.*;
import java.util.List;

@Stateless
@RolesAllowed({"ADMIN", "OPERADOR"})
public class ClienteService {

  @PersistenceContext
  private EntityManager em;

  public void salvar(Cliente c) {
    em.persist(c);
  }

  public List<Cliente> listar() {
    return em.createQuery("from Cliente", Cliente.class).getResultList();
  }
}
