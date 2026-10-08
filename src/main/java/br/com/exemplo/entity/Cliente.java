
package br.com.exemplo.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;

@Entity
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Identificador gerado pelo banco", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
  private Long id;
  @Schema(example = "Maria Silva")
  private String nome;
  @Schema(example = "maria@exemplo.com")
  private String email;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getNome() { return nome; }
  public void setNome(String nome) { this.nome = nome; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
}
