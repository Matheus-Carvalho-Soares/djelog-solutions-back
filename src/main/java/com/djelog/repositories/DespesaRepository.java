package com.djelog.repositories;

import com.djelog.entities.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, UUID> {
    List<Despesa> findByViagem_Id(UUID viagemId);

    @Query("SELECT d FROM Despesa d JOIN FETCH d.viagem v WHERE v.id = :viagemId")
    List<Despesa> findByViagemIdWithViagem(@Param("viagemId") UUID viagemId);

    @Query("SELECT d FROM Despesa d JOIN FETCH d.viagem v JOIN FETCH v.profissional p WHERE p.usuario.id = :usuarioId")
    List<Despesa> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
