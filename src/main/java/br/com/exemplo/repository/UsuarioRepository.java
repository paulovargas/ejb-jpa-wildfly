package br.com.exemplo.repository;

import br.com.exemplo.entity.Usuario;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Locale;
import java.util.Optional;

@Stateless
@PermitAll
public class UsuarioRepository {
  @PersistenceContext(unitName = "MeuPU")
  private EntityManager em;

  public Optional<Usuario> buscarPorEmail(String email) {
    return em.createQuery("select u from Usuario u where u.email = :email", Usuario.class)
        .setParameter("email", email.trim().toLowerCase(Locale.ROOT)).getResultStream().findFirst();
  }

  public Optional<Usuario> buscarPorId(Long id) {
    return Optional.ofNullable(em.find(Usuario.class, id));
  }

  public void salvar(Usuario usuario) {
    em.persist(usuario);
    em.flush();
  }

}
