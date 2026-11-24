package br.com.meunegocio360.core.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "categoria")
public class Categoria {

  @Id
  @UuidGenerator
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "empresa_id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID empresaId;

  @Column(nullable = false, length = 80)
  private String nome;

  @Column(nullable = false)
  private boolean ativo;

  @Column(name = "criado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  protected Categoria() {}

  public Categoria(UUID empresaId, String nome, Instant agora) {
    this.empresaId = empresaId;
    this.nome = nome;
    this.ativo = true;
    this.criadoEm = agora;
    this.atualizadoEm = agora;
  }

  public void renomear(String nome, Instant agora) {
    if (!this.nome.equals(nome)) {
      this.nome = nome;
      this.atualizadoEm = agora;
    }
  }

  public void alterarSituacao(boolean ativo, Instant agora) {
    if (this.ativo != ativo) {
      this.ativo = ativo;
      this.atualizadoEm = agora;
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getEmpresaId() {
    return empresaId;
  }

  public String getNome() {
    return nome;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public Instant getCriadoEm() {
    return criadoEm;
  }

  public Instant getAtualizadoEm() {
    return atualizadoEm;
  }

  public long getVersao() {
    return versao;
  }
}
