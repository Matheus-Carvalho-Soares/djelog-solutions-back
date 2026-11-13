package com.djelog.repositories;

import com.djelog.entities.PagamentoMotoristaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PagamentoMotoristaItemRepository extends JpaRepository<PagamentoMotoristaItem, UUID> {
    @Query("select i.viagem.id, sum(i.valor) from PagamentoMotoristaItem i " +
            "where i.viagem.id in :viagemIds group by i.viagem.id")
    List<Object[]> sumByViagemIds(@Param("viagemIds") Collection<UUID> viagemIds);

    @Query("select coalesce(sum(i.valor), 0) from PagamentoMotoristaItem i where i.viagem.id = :viagemId")
    BigDecimal sumByViagemId(@Param("viagemId") UUID viagemId);

    @Query("select coalesce(sum(i.valor), 0) from PagamentoMotoristaItem i where i.controle.id = :controleId")
    BigDecimal sumByControleId(@Param("controleId") UUID controleId);

    boolean existsByViagem_Id(UUID viagemId);
}
