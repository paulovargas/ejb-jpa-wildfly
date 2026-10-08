package br.com.exemplo.security;

import br.com.exemplo.exception.ApiException;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginRateLimiterTest {
  @Test void deveLimitarTentativasPorEnderecoELiberarAposUmMinuto() {
    Clock clock = mock(Clock.class);
    Instant agora = Instant.parse("2026-10-08T12:00:00Z");
    when(clock.instant()).thenReturn(agora);
    LoginRateLimiter limiter = new LoginRateLimiter(clock);
    for (int i = 0; i < 10; i++) { limiter.verificar("127.0.0.1"); }
    ApiException erro = assertThrows(ApiException.class, () -> limiter.verificar("127.0.0.1"));
    assertEquals(429, erro.getStatus());
    assertDoesNotThrow(() -> limiter.verificar("127.0.0.2"));
    when(clock.instant()).thenReturn(agora.plusSeconds(60));
    assertDoesNotThrow(() -> limiter.verificar("127.0.0.1"));
  }
}
