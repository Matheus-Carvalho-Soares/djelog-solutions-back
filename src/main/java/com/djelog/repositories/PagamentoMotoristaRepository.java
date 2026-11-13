package com.djelog.repositories;

import com.djelog.entities.PagamentoMotorista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PagamentoMotoristaRepository extends JpaRepository<PagamentoMotorista, UUID> {
    @Query("select distinct p from PagamentoMotorista p left join fetch p.itens i left join fetch i.viagem v left join fetch v.veiculo " +
            "where p.profissional.id = :profissionalId and p.usuario.id = :usuarioId " +
            "order by p.dataPagamento desc, p.criadoEm desc")
    List<PagamentoMotorista> findDetailsByProfessional(@Param("profissionalId") UUID profissionalId,
            @Param("usuarioId") UUID usuarioId);

    @Query("select distinct p from PagamentoMotorista p left join fetch p.itens i left join fetch i.viagem v left join fetch v.veiculo " +
            "where p.profissional.id = :profissionalId and p.usuario.id = :usuarioId " +
            "and p.dataPagamento >= :inicio and p.dataPagamento <= :fim " +
            "order by p.dataPagamento desc, p.criadoEm desc")
    List<PagamentoMotorista> findDetailsByPeriod(@Param("profissionalId") UUID profissionalId,
            @Param("usuarioId") UUID usuarioId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
