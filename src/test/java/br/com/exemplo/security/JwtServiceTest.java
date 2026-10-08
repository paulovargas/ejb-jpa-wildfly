package br.com.exemplo.security;

import br.com.exemplo.entity.Perfil;
import br.com.exemplo.entity.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
  private final byte[] segredo = new byte[32];
  private final Instant agora = Instant.parse("2026-10-08T12:00:00Z");
  private final Clock clock = Clock.fixed(agora, ZoneOffset.UTC);
  private final JwtService jwt = new JwtService(segredo, 900, clock);

  private Usuario usuario() {
    Usuario usuario = new Usuario();
    usuario.setId(42L);
    usuario.setPerfis(Collections.singleton(Perfil.OPERADOR));
    return usuario;
  }

  private String token(String issuer, String audience, String subject, Instant expiracao) {
    return Jwts.builder().issuer(issuer).audience().add(audience).and().subject(subject)
        .issuedAt(Date.from(agora)).expiration(Date.from(expiracao))
        .signWith(new SecretKeySpec(segredo, "HmacSHA256"), Jwts.SIG.HS256).compact();
  }

  @Test void deveEmitirTokenComIdentidadeEValidade() {
    assertEquals(42L, jwt.validar(jwt.emitir(usuario())));
    assertEquals(900, jwt.getExpiresIn());
  }

  @Test void deveRejeitarTokenAssinadoComOutraChave() {
    byte[] outra = new byte[32];
    outra[0] = 1;
    String token = new JwtService(outra, 900, clock).emitir(usuario());
    assertThrows(JwtException.class, () -> jwt.validar(token));
  }

  @Test void deveRejeitarTokenExpiradoInclusiveNoInstanteDeExpiracao() {
    String token = jwt.emitir(usuario());
    JwtService futuro = new JwtService(segredo, 900, Clock.fixed(agora.plusSeconds(900), ZoneOffset.UTC));
    assertThrows(JwtException.class, () -> futuro.validar(token));
  }

  @Test void deveRejeitarIssuerEAudienceIncorretos() {
    assertThrows(JwtException.class, () -> jwt.validar(token("outro", JwtService.AUDIENCE, "42", agora.plusSeconds(900))));
    assertThrows(JwtException.class, () -> jwt.validar(token(JwtService.ISSUER, "outro", "42", agora.plusSeconds(900))));
  }

  @Test void deveRejeitarTokenSemExpiracao() {
    String token = Jwts.builder().issuer(JwtService.ISSUER).audience().add(JwtService.AUDIENCE).and()
        .subject("42").issuedAt(Date.from(agora))
        .signWith(new SecretKeySpec(segredo, "HmacSHA256"), Jwts.SIG.HS256).compact();
    assertThrows(JwtException.class, () -> jwt.validar(token));
  }

  @Test void deveRejeitarIdentidadeInvalida() {
    for (String subject : new String[]{"email@exemplo.com", "0", "-1"}) {
      assertThrows(JwtException.class, () -> jwt.validar(token(JwtService.ISSUER, JwtService.AUDIENCE, subject, agora.plusSeconds(900))));
    }
  }

  @Test void deveRejeitarTokenEmitidoNoFuturo() {
    String token = new JwtService(segredo, 900, Clock.fixed(agora.plusSeconds(60), ZoneOffset.UTC)).emitir(usuario());
    assertThrows(JwtException.class, () -> jwt.validar(token));
  }

  @Test void deveRejeitarAlgoritmoDiferenteETokenSemAssinatura() {
    String token = Jwts.builder().subject("42").signWith(new SecretKeySpec(new byte[64], "HmacSHA512"), Jwts.SIG.HS512).compact();
    assertThrows(JwtException.class, () -> jwt.validar(token));
    assertThrows(JwtException.class, () -> jwt.validar(Jwts.builder().subject("42").compact()));
  }

  @Test void deveRejeitarConfiguracaoFraca() {
    assertThrows(IllegalArgumentException.class, () -> new JwtService(new byte[16], 900, clock));
    assertThrows(IllegalArgumentException.class, () -> new JwtService(segredo, 0, clock));
    assertThrows(IllegalArgumentException.class, () -> new JwtService(segredo, 3601, clock));
  }
}
