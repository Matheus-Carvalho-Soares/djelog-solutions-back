package com.djelog.repositories;

import com.djelog.entities.Veiculo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {
    @EntityGraph(attributePaths = "profissional")
    List<Veiculo> findByProfissional_Usuario_Id(UUID usuarioId);

    @EntityGraph(attributePaths = "profissional")
    Optional<Veiculo> findByIdAndProfissional_Usuario_Id(UUID id, UUID usuarioId);

    @EntityGraph(attributePaths = {"profissional", "proprietario", "caminhao"})
    @Query("select v from Veiculo v left join v.profissional p left join v.proprietario o where p.usuario.id = :usuarioId or o.usuario.id = :usuarioId")
    List<Veiculo> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @EntityGraph(attributePaths = {"profissional", "proprietario", "caminhao"})
    @Query("select v from Veiculo v left join v.profissional p left join v.proprietario o where v.id = :id and (p.usuario.id = :usuarioId or o.usuario.id = :usuarioId)")
    Optional<Veiculo> findByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("select count(v) > 0 from Veiculo v left join v.profissional p left join v.proprietario o where v.id = :id and (p.usuario.id = :usuarioId or o.usuario.id = :usuarioId)")
    boolean existsByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    boolean existsByCaminhao_Id(UUID caminhaoId);
    boolean existsByCaminhao_IdAndIdNot(UUID caminhaoId, UUID id);
}
