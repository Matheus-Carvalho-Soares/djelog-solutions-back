package com.djelog.repositories;

import com.djelog.entities.ViagemComissionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ViagemComissionadaRepository extends JpaRepository<ViagemComissionada, UUID> {
    List<ViagemComissionada> findByUsuario_Id(UUID usuarioId);
    Optional<ViagemComissionada> findByIdAndUsuario_Id(UUID id, UUID usuarioId);
    boolean existsByIdAndUsuario_Id(UUID id, UUID usuarioId);

    List<ViagemComissionada> findByInicioFreteContainingAndUsuario_Id(String inicioFrete, UUID usuarioId);
    
    List<ViagemComissionada> findByFimFreteContainingAndUsuario_Id(String fimFrete, UUID usuarioId);
    
    List<ViagemComissionada> findByValor(Integer valor);
    
    List<ViagemComissionada> findByComissao(Integer comissao);
}
