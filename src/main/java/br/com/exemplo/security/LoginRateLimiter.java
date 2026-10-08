package br.com.exemplo.security;

import br.com.exemplo.exception.ApiException;
import javax.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class LoginRateLimiter {
  private final Map<String, Janela> tentativas = new HashMap<>();
  private Clock clock = Clock.systemUTC();

  public LoginRateLimiter() {}
  LoginRateLimiter(Clock clock) { this.clock = clock; }

  public synchronized void verificar(String endereco) {
    Instant agora = clock.instant();
    Janela janela = tentativas.get(endereco);
    if (janela == null || !agora.isBefore(janela.inicio.plusSeconds(60))) {
      if (tentativas.size() >= 10000) {
        tentativas.entrySet().removeIf(e -> !agora.isBefore(e.getValue().inicio.plusSeconds(60)));
        if (tentativas.size() >= 10000) { rejeitar(); }
      }
      janela = new Janela(agora);
      tentativas.put(endereco, janela);
    }
    if (janela.quantidade >= 10) { rejeitar(); }
    janela.quantidade++;
  }

  private void rejeitar() {
    throw new ApiException(429, "Muitas tentativas de login. Aguarde um minuto.");
  }

  private static class Janela {
    private final Instant inicio;
    private int quantidade;
    private Janela(Instant inicio) { this.inicio = inicio; }
  }
}
