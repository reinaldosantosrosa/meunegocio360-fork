package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.domain.TipoItemCatalogo;
import java.time.Instant;
import java.util.UUID;

public record ItemCatalogoResponse(
    UUID id,
    TipoItemCatalogo tipo,
    String codigo,
    String nome,
    String descricao,
    String unidade,
    String preco,
    CategoriaResumo categoria,
    boolean ativo,
    Instant criadoEm,
    Instant atualizadoEm,
    long versao) {

  public record CategoriaResumo(UUID id, String nome) {}
}
