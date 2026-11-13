package com.djelog.repositories;

import com.djelog.entities.ControleFinanceiroMotorista;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ControleFinanceiroMotoristaRepository extends JpaRepository<ControleFinanceiroMotorista, UUID> {
    Optional<ControleFinanceiroMotorista> findByProfissional_IdAndUsuario_Id(UUID profissionalId, UUID usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ControleFinanceiroMotorista c where c.profissional.id = :profissionalId and c.usuario.id = :usuarioId")
    Optional<ControleFinanceiroMotorista> findByProfissionalAndUsuarioForUpdate(
            @Param("profissionalId") UUID profissionalId, @Param("usuarioId") UUID usuarioId);
}
