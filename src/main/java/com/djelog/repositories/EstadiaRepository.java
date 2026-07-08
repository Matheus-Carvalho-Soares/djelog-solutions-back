package com.djelog.repositories;

import com.djelog.entities.Estadia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadiaRepository extends JpaRepository<Estadia, UUID> {
    List<Estadia> findByViagem_Id(UUID viagemId);

    @Query("SELECT e FROM Estadia e JOIN FETCH e.viagem v WHERE v.id = :viagemId")
    List<Estadia> findByViagemIdWithViagem(@Param("viagemId") UUID viagemId);

    @Query("SELECT e FROM Estadia e JOIN FETCH e.viagem v WHERE v.id IN :viagemIds")
    List<Estadia> findByViagemIdsWithViagem(@Param("viagemIds") List<UUID> viagemIds);

    @Query("SELECT e FROM Estadia e JOIN FETCH e.viagem v JOIN FETCH v.profissional p WHERE p.usuario.id = :usuarioId")
    List<Estadia> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT e FROM Estadia e JOIN FETCH e.viagem v JOIN FETCH v.profissional p WHERE e.id = :id AND p.usuario.id = :usuarioId")
    Optional<Estadia> findByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT e FROM Estadia e JOIN FETCH e.viagem v JOIN FETCH v.profissional p WHERE v.id = :viagemId AND p.usuario.id = :usuarioId")
    List<Estadia> findByViagemIdAndUsuarioId(@Param("viagemId") UUID viagemId, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(e) > 0 FROM Estadia e JOIN e.viagem v JOIN v.profissional p WHERE e.id = :id AND p.usuario.id = :usuarioId")
    boolean existsByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);
}
