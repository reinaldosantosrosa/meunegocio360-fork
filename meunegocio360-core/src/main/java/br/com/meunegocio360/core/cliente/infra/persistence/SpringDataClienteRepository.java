package br.com.meunegocio360.core.cliente.infra.persistence;

import br.com.meunegocio360.core.cliente.domain.Cliente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataClienteRepository extends JpaRepository<Cliente, UUID> {

  @Query(
      value =
          """
          select cliente from Cliente cliente
          where cliente.empresaId = :empresaId
            and (:ativo is null or cliente.ativo = :ativo)
            and (:nome is null or cliente.nome like concat('%', :nome, '%')
              or (:documento is not null and cliente.documento like concat('%', :documento, '%')))
          """,
      countQuery =
          """
          select count(cliente) from Cliente cliente
          where cliente.empresaId = :empresaId
            and (:ativo is null or cliente.ativo = :ativo)
            and (:nome is null or cliente.nome like concat('%', :nome, '%')
              or (:documento is not null and cliente.documento like concat('%', :documento, '%')))
          """)
  Page<Cliente> pesquisar(
      @Param("empresaId") UUID empresaId,
      @Param("nome") String nome,
      @Param("documento") String documento,
      @Param("ativo") Boolean ativo,
      Pageable pageable);

  Optional<Cliente> findByIdAndEmpresaId(UUID id, UUID empresaId);

  boolean existsByEmpresaIdAndDocumento(UUID empresaId, String documento);
}
