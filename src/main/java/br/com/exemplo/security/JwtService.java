package br.com.exemplo.security;

import br.com.exemplo.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class JwtService {
  static final String ISSUER = "ejb-jpa-wildfly";
  static final String AUDIENCE = "ejb-jpa-wildfly-api";
  private SecretKey chave;
  private long expiresIn;
  private Clock clock = Clock.systemUTC();

  public JwtService() {}

  JwtService(byte[] segredo, long expiresIn, Clock clock) {
    configurar(segredo, expiresIn);
    this.clock = clock;
  }

  @PostConstruct
  public void inicializar() {
    String segredo = System.getenv("JWT_SECRET_BASE64");
    if (segredo == null || segredo.isBlank()) {
      throw new IllegalStateException("Configure JWT_SECRET_BASE64 antes de iniciar o servidor.");
    }
    String ttl = System.getenv("JWT_TTL_SECONDS");
    try {
      configurar(Base64.getDecoder().decode(segredo), ttl == null ? 900 : Long.parseLong(ttl));
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Configuração JWT inválida: use uma chave Base64 de 32 bytes e TTL entre 60 e 3600 segundos.");
    }
  }

  private void configurar(byte[] segredo, long ttl) {
    if (segredo.length != 32 || ttl < 60 || ttl > 3600) {
      throw new IllegalArgumentException("Chave ou validade JWT inválida.");
    }
    chave = new SecretKeySpec(segredo, "HmacSHA256");
    expiresIn = ttl;
  }

  public String emitir(Usuario usuario) {
    Instant agora = clock.instant();
    return Jwts.builder()
        .issuer(ISSUER).audience().add(AUDIENCE).and()
        .subject(usuario.getId().toString())
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(expiresIn)))
        .claim("roles", usuario.getPerfis().stream().map(Enum::name).sorted().collect(Collectors.toList()))
        .signWith(chave, Jwts.SIG.HS256).compact();
  }

  public Long validar(String token) {
    Jws<Claims> assinatura = Jwts.parser().verifyWith(chave)
        .requireIssuer(ISSUER).requireAudience(AUDIENCE)
        .clock(() -> Date.from(clock.instant())).build()
        .parseSignedClaims(token);
    if (!"HS256".equals(assinatura.getHeader().getAlgorithm())) {
      throw new JwtException("Algoritmo JWT não permitido.");
    }
    Claims claims = assinatura.getPayload();
    if (claims.getExpiration() == null || claims.getIssuedAt() == null
        || !claims.getExpiration().after(claims.getIssuedAt())
        || !claims.getExpiration().toInstant().isAfter(clock.instant())
        || claims.getIssuedAt().toInstant().isAfter(clock.instant())) {
      throw new JwtException("Datas JWT inválidas.");
    }
    try {
      long id = Long.parseLong(claims.getSubject());
      if (id <= 0) { throw new NumberFormatException(); }
      return id;
    } catch (NumberFormatException e) {
      throw new JwtException("Identidade JWT inválida.");
    }
  }

  public long getExpiresIn() { return expiresIn; }
}
