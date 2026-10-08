package br.com.exemplo.security;

import br.com.exemplo.dto.CriarUsuarioRequest;
import br.com.exemplo.entity.Perfil;
import br.com.exemplo.entity.Usuario;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Validator;
import java.util.Collections;

@Singleton
@Startup
public class UsuarioInicial {
  @PersistenceContext(unitName = "MeuPU")
  private EntityManager em;
  @Inject
  private SenhaService senhas;
  @Inject
  private JwtService jwt;
  @Inject
  private Validator validator;

  @PostConstruct
  public void inicializar() {
    // Inicializa a configuração JWT durante o deploy, inclusive em bancos já populados.
    jwt.getExpiresIn();
    if (em.createQuery("select count(u) from Usuario u", Long.class).getSingleResult() > 0) { return; }
    String email = System.getenv("APP_ADMIN_EMAIL");
    String senha = System.getenv("APP_ADMIN_PASSWORD");
    if (email == null || senha == null) {
      throw new IllegalStateException("Banco sem usuários: configure APP_ADMIN_EMAIL e APP_ADMIN_PASSWORD para o primeiro deploy.");
    }
    CriarUsuarioRequest request = new CriarUsuarioRequest();
    request.setNome("Administrador");
    request.setEmail(email);
    request.setSenha(senha);
    request.setPerfis(Collections.singleton(Perfil.ADMIN));
    if (!validator.validate(request).isEmpty()) {
      throw new IllegalStateException("Administrador inicial inválido: informe email válido e senha de 12 a 128 caracteres.");
    }
    Usuario usuario = new Usuario();
    usuario.setNome(request.getNome());
    usuario.setEmail(email);
    usuario.setSenhaHash(senhas.gerar(senha));
    usuario.setPerfis(request.getPerfis());
    em.persist(usuario);
  }
}
