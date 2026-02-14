package com.djelog.repositories;

import com.djelog.entities.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ViagemRepository extends JpaRepository<Viagem, UUID> {
    List<Viagem> findByProfissional_Usuario_Id(UUID usuarioId);

    @Query("""
            select v
            from Viagem v
            join fetch v.profissional p
            join fetch v.empresa e
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
}
