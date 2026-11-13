package com.djelog.repositories;

import com.djelog.entities.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, UUID> {
    List<Despesa> findByViagem_Id(UUID viagemId);

    @Query("SELECT d FROM Despesa d JOIN FETCH d.viagem v WHERE v.id = :viagemId")
    List<Despesa> findByViagemIdWithViagem(@Param("viagemId") UUID viagemId);

    @Query("SELECT d FROM Despesa d JOIN FETCH d.viagem v WHERE v.id IN :viagemIds")
    List<Despesa> findByViagemIdsWithViagem(@Param("viagemIds") List<UUID> viagemIds);

    @Query("SELECT DISTINCT d FROM Despesa d LEFT JOIN FETCH d.viagem v LEFT JOIN FETCH v.profissional p LEFT JOIN FETCH d.veiculo ve LEFT JOIN FETCH d.parcelas pa WHERE d.usuario.id = :usuarioId OR p.usuario.id = :usuarioId")
    List<Despesa> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT DISTINCT d FROM Despesa d LEFT JOIN FETCH d.viagem v LEFT JOIN FETCH v.profissional p LEFT JOIN FETCH d.veiculo ve LEFT JOIN FETCH d.parcelas pa WHERE d.id = :id AND (d.usuario.id = :usuarioId OR p.usuario.id = :usuarioId)")
    Optional<Despesa> findByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT d FROM Despesa d LEFT JOIN FETCH d.viagem v LEFT JOIN FETCH v.profissional p LEFT JOIN FETCH d.veiculo ve LEFT JOIN FETCH d.parcelas pa WHERE d.id = :id AND (d.usuario.id = :usuarioId OR p.usuario.id = :usuarioId)")
    Optional<Despesa> findByIdAndUsuarioIdForUpdate(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT DISTINCT d FROM Despesa d LEFT JOIN FETCH d.viagem v LEFT JOIN FETCH d.parcelas pa WHERE v.id = :viagemId AND (d.usuario.id = :usuarioId OR v.profissional.usuario.id = :usuarioId)")
    List<Despesa> findByViagemIdAndUsuarioId(@Param("viagemId") UUID viagemId, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(d) > 0 FROM Despesa d LEFT JOIN d.viagem v LEFT JOIN v.profissional p WHERE d.id = :id AND (d.usuario.id = :usuarioId OR p.usuario.id = :usuarioId)")
    boolean existsByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(d) > 0 FROM Despesa d LEFT JOIN d.viagem v LEFT JOIN v.profissional p WHERE v.id = :viagemId AND (d.usuario.id = :usuarioId OR p.usuario.id = :usuarioId)")
    boolean existsByViagemIdAndUsuarioId(@Param("viagemId") UUID viagemId, @Param("usuarioId") UUID usuarioId);
}
