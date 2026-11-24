package br.com.meunegocio360.core.catalogo.infra.persistence;

import br.com.meunegocio360.core.catalogo.domain.Categoria;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCategoriaRepository extends JpaRepository<Categoria, UUID> {

  @Query(
      value =
          """
          select c from Categoria c
          where c.empresaId = :empresaId
            and (:ativo is null or c.ativo = :ativo)
            and (:q is null or c.nome like concat('%', :q, '%'))
          """,
      countQuery =
          """
          select count(c) from Categoria c
          where c.empresaId = :empresaId
            and (:ativo is null or c.ativo = :ativo)
            and (:q is null or c.nome like concat('%', :q, '%'))
          """)
  Page<Categoria> listar(
      @Param("empresaId") UUID empresaId,
      @Param("ativo") Boolean ativo,
      @Param("q") String busca,
      Pageable pageable);

  Optional<Categoria> findByIdAndEmpresaId(UUID id, UUID empresaId);

  Optional<Categoria> findByNomeAndEmpresaId(String nome, UUID empresaId);

  Optional<Categoria> findByNomeAndEmpresaIdAndIdNot(String nome, UUID empresaId, UUID id);
}
