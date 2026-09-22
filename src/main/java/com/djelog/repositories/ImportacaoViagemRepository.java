package com.djelog.repositories;

import com.djelog.entities.ImportacaoViagem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ImportacaoViagemRepository extends JpaRepository<ImportacaoViagem, UUID> {
    Optional<ImportacaoViagem> findByIdAndUsuario_Id(UUID id, UUID usuarioId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ImportacaoViagem i where i.id = :id and i.usuario.id = :usuarioId")
    Optional<ImportacaoViagem> lockByIdAndUsuario(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);
}
