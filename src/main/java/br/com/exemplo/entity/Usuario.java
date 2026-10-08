package br.com.exemplo.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"))
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, length = 100)
  private String nome;
  @Column(nullable = false, length = 254)
  private String email;
  @Column(name = "senha_hash", nullable = false, length = 255)
  private String senhaHash;
  @Column(nullable = false)
  private boolean ativo = true;
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "usuario_perfis", joinColumns = @JoinColumn(name = "usuario_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "perfil"}))
  @Enumerated(EnumType.STRING)
  @Column(name = "perfil", nullable = false, length = 20)
  private Set<Perfil> perfis = new HashSet<>();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getNome() { return nome; }
  public void setNome(String nome) { this.nome = nome; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email.trim().toLowerCase(Locale.ROOT); }
  public String getSenhaHash() { return senhaHash; }
  public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
  public boolean isAtivo() { return ativo; }
  public void setAtivo(boolean ativo) { this.ativo = ativo; }
  public Set<Perfil> getPerfis() { return perfis; }
  public void setPerfis(Set<Perfil> perfis) { this.perfis = new HashSet<>(perfis); }
}
