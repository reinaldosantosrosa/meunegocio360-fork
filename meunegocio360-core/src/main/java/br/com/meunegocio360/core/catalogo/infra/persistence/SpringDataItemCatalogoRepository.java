package br.com.meunegocio360.core.catalogo.infra.persistence;

import br.com.meunegocio360.core.catalogo.domain.ItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.TipoItemCatalogo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataItemCatalogoRepository extends JpaRepository<ItemCatalogo, UUID> {

  @EntityGraph(attributePaths = "categoria")
  @Query(
      value =
          """
          select i from ItemCatalogo i
          left join i.categoria c
          where i.empresaId = :empresaId
            and (:ativo is null or i.ativo = :ativo)
            and (:tipo is null or i.tipo = :tipo)
            and (:categoriaId is null or c.id = :categoriaId)
            and (:q is null or i.nome like concat('%', :q, '%')
              or i.codigo like concat('%', :q, '%'))
          """,
      countQuery =
          """
          select count(i) from ItemCatalogo i
          left join i.categoria c
          where i.empresaId = :empresaId
            and (:ativo is null or i.ativo = :ativo)
            and (:tipo is null or i.tipo = :tipo)
            and (:categoriaId is null or c.id = :categoriaId)
            and (:q is null or i.nome like concat('%', :q, '%')
              or i.codigo like concat('%', :q, '%'))
          """)
  Page<ItemCatalogo> listar(
      @Param("empresaId") UUID empresaId,
      @Param("ativo") Boolean ativo,
      @Param("tipo") TipoItemCatalogo tipo,
      @Param("categoriaId") UUID categoriaId,
      @Param("q") String busca,
      Pageable pageable);

  @EntityGraph(attributePaths = "categoria")
  Optional<ItemCatalogo> findByIdAndEmpresaId(UUID id, UUID empresaId);

  Optional<ItemCatalogo> findByCodigoAndEmpresaId(String codigo, UUID empresaId);

  Optional<ItemCatalogo> findByCodigoAndEmpresaIdAndIdNot(String codigo, UUID empresaId, UUID id);

  long countByCategoria_IdAndEmpresaId(UUID categoriaId, UUID empresaId);
}
