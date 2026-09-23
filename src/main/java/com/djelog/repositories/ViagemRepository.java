package com.djelog.repositories;

import com.djelog.entities.Viagem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ViagemRepository extends JpaRepository<Viagem, UUID> {
    @EntityGraph(attributePaths = {"profissional", "empresa", "veiculo"})
    List<Viagem> findByProfissional_Usuario_Id(UUID usuarioId);

    @EntityGraph(attributePaths = {"profissional", "empresa", "veiculo"})
    Optional<Viagem> findByIdAndProfissional_Usuario_Id(UUID id, UUID usuarioId);

    boolean existsByIdAndProfissional_Usuario_Id(UUID id, UUID usuarioId);

    @Query("""
            select v
            from Viagem v
            join fetch v.profissional p
            left join fetch v.empresa e
            join fetch v.veiculo ve
            where p.usuario.id = :usuarioId
              and v.dataInicio <= :dataFim
              and (v.dataFim is null or v.dataFim >= :dataInicio)
            order by v.dataInicio asc
            """)
    List<Viagem> findByPeriodoSobreposto(
            @Param("usuarioId") UUID usuarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    @Query("""
            select v
            from Viagem v
            join fetch v.profissional p
            left join fetch v.empresa e
            join fetch v.veiculo ve
            where p.usuario.id = :usuarioId
              and v.dataInicio <= :dataFim
              and (v.dataFim is null or v.dataFim >= :dataInicio)
              and (:filtraVeiculos = false or ve.id in :veiculoIds)
              and (:filtraProfissionais = false or p.id in :profissionalIds)
              and (:filtraEmpresas = false or e.id in :empresaIds)
              and (:filtraStatus = false or v.status in :status)
            order by v.dataInicio asc
            """)
    List<Viagem> findByPeriodoSobrepostoAndFiltros(
            @Param("usuarioId") UUID usuarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("veiculoIds") List<UUID> veiculoIds,
            @Param("filtraVeiculos") boolean filtraVeiculos,
            @Param("profissionalIds") List<UUID> profissionalIds,
            @Param("filtraProfissionais") boolean filtraProfissionais,
            @Param("empresaIds") List<UUID> empresaIds,
            @Param("filtraEmpresas") boolean filtraEmpresas,
            @Param("status") List<String> status,
            @Param("filtraStatus") boolean filtraStatus
    );
}
