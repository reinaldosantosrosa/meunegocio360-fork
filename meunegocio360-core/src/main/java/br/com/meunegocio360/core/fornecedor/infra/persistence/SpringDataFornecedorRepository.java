package br.com.meunegocio360.core.fornecedor.infra.persistence;

import br.com.meunegocio360.core.fornecedor.domain.Fornecedor;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataFornecedorRepository extends JpaRepository<Fornecedor, UUID> {

  @Query(
      value =
          "select f from Fornecedor f where f.empresaId = :empresaId "
              + "and (:ativo is null or f.ativo = :ativo) "
              + "and (:busca is null or lower(f.nome) like lower(concat('%', :busca, '%')) "
              + "or (:buscaDocumento is not null and f.documento like concat('%', :buscaDocumento, '%')))",
      countQuery =
          "select count(f) from Fornecedor f where f.empresaId = :empresaId "
              + "and (:ativo is null or f.ativo = :ativo) "
              + "and (:busca is null or lower(f.nome) like lower(concat('%', :busca, '%')) "
              + "or (:buscaDocumento is not null and f.documento like concat('%', :buscaDocumento, '%')))")
  Page<Fornecedor> listar(
      @Param("empresaId") UUID empresaId,
      @Param("ativo") Boolean ativo,
      @Param("busca") String busca,
      @Param("buscaDocumento") String buscaDocumento,
      Pageable pageable);

  Optional<Fornecedor> findByIdAndEmpresaId(UUID id, UUID empresaId);

  Optional<Fornecedor> findByDocumentoAndEmpresaId(String documento, UUID empresaId);

  Optional<Fornecedor> findByDocumentoAndEmpresaIdAndIdNot(
      String documento, UUID empresaId, UUID id);
}
