package br.com.meunegocio360.core.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "item_catalogo")
public class ItemCatalogo {

  @Id
  @UuidGenerator
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "empresa_id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID empresaId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoria_id")
  private Categoria categoria;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TipoItemCatalogo tipo;

  @Column(length = 40)
  private String codigo;

  @Column(nullable = false, length = 150)
  private String nome;

  @Column(length = 500)
  private String descricao;

  @Column(nullable = false, length = 6)
  private String unidade;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal preco;

  @Column(nullable = false)
  private boolean ativo;

  @Column(name = "criado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  protected ItemCatalogo() {}

  public ItemCatalogo(
      UUID empresaId,
      Categoria categoria,
      TipoItemCatalogo tipo,
      String codigo,
      String nome,
      String descricao,
      String unidade,
      BigDecimal preco,
      Instant agora) {
    this.empresaId = empresaId;
    this.categoria = categoria;
    this.tipo = tipo;
    this.codigo = codigo;
    this.nome = nome;
    this.descricao = descricao;
    this.unidade = unidade;
    this.preco = preco;
    this.ativo = true;
    this.criadoEm = agora;
    this.atualizadoEm = agora;
  }

  public void atualizar(
      Categoria categoria,
      String codigo,
      String nome,
      String descricao,
      String unidade,
      BigDecimal preco,
      Instant agora) {
    this.categoria = categoria;
    this.codigo = codigo;
    this.nome = nome;
    this.descricao = descricao;
    this.unidade = unidade;
    this.preco = preco;
    this.atualizadoEm = agora;
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

  public Categoria getCategoria() {
    return categoria;
  }

  public TipoItemCatalogo getTipo() {
    return tipo;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNome() {
    return nome;
  }

  public String getDescricao() {
    return descricao;
  }

  public String getUnidade() {
    return unidade;
  }

  public BigDecimal getPreco() {
    return preco;
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
