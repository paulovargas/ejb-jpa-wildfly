package br.com.exemplo.security;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SenhaService {
  @Inject
  private Pbkdf2PasswordHash passwordHash;
  private String hashInexistente;

  @PostConstruct
  public void inicializar() {
    passwordHash.initialize(Map.of(
        "Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA256",
        "Pbkdf2PasswordHash.Iterations", "600000",
        "Pbkdf2PasswordHash.SaltSizeBytes", "32",
        "Pbkdf2PasswordHash.KeySizeBytes", "32"));
    hashInexistente = gerar(UUID.randomUUID().toString());
  }

  public String gerar(String senha) {
    char[] caracteres = senha.toCharArray();
    try {
      return passwordHash.generate(caracteres);
    } finally {
      Arrays.fill(caracteres, '\0');
    }
  }

  public boolean verificar(String senha, String hash) {
    char[] caracteres = senha.toCharArray();
    try {
      // Mesmo usuários inexistentes passam pelo cálculo do hash.
      boolean valido = passwordHash.verify(caracteres, hash == null ? hashInexistente : hash);
      return hash != null && valido;
    } finally {
      Arrays.fill(caracteres, '\0');
    }
  }
}
