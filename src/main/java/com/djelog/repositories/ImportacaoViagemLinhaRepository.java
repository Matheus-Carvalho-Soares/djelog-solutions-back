package com.djelog.repositories;

import com.djelog.entities.ImportacaoViagemLinha;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ImportacaoViagemLinhaRepository extends JpaRepository<ImportacaoViagemLinha, UUID> {
    List<ImportacaoViagemLinha> findByImportacao_IdOrderByNumeroLinha(UUID importacaoId);
    List<ImportacaoViagemLinha> findByImportacao_Usuario_IdAndViagemIsNotNull(UUID usuarioId);
}
