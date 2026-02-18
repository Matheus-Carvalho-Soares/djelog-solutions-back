package com.djelog.repositories;

import com.djelog.entities.Estadia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EstadiaRepository extends JpaRepository<Estadia, UUID> {
    List<Estadia> findByViagem_Id(UUID viagemId);

    @Query("SELECT e FROM Estadia e JOIN FETCH e.viagem v WHERE v.id = :viagemId")
    List<Estadia> findByViagemIdWithViagem(@Param("viagemId") UUID viagemId);

    @Query("SELECT e FROM Estadia e JOIN FETCH e.viagem v JOIN FETCH v.profissional p WHERE p.usuario.id = :usuarioId")
    List<Estadia> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
