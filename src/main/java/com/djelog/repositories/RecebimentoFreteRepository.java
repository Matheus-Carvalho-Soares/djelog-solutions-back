package com.djelog.repositories;

import com.djelog.entities.RecebimentoFrete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

public interface RecebimentoFreteRepository extends JpaRepository<RecebimentoFrete, UUID> {
    @Query("select r from RecebimentoFrete r join fetch r.viagem v join fetch v.profissional p where v.id = :viagemId and p.usuario.id = :usuarioId order by r.dataRecebimento, r.id")
    List<RecebimentoFrete> findByViagemIdAndUsuarioId(@Param("viagemId") UUID viagemId, @Param("usuarioId") UUID usuarioId);

    @Query("select r from RecebimentoFrete r join fetch r.viagem v join fetch v.profissional p where r.id = :id and p.usuario.id = :usuarioId")
    Optional<RecebimentoFrete> findByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("select count(r) > 0 from RecebimentoFrete r where r.viagem.id = :viagemId")
    boolean existsByViagemId(@Param("viagemId") UUID viagemId);

    @Query("select coalesce(sum(r.valor), 0) from RecebimentoFrete r where r.viagem.id = :viagemId")
    BigDecimal sumByViagemId(@Param("viagemId") UUID viagemId);

    @Query("select coalesce(sum(r.valor), 0) from RecebimentoFrete r where r.viagem.id = :viagemId and r.inicial = true")
    BigDecimal sumInicialByViagemId(@Param("viagemId") UUID viagemId);

    @Query("select r.viagem.id, coalesce(sum(r.valor), 0), coalesce(sum(case when r.inicial = true then r.valor else 0 end), 0) " +
            "from RecebimentoFrete r where r.viagem.id in :viagemIds group by r.viagem.id")
    List<Object[]> totalsByViagemIds(@Param("viagemIds") Collection<UUID> viagemIds);
}
