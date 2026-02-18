package com.djelog.repositories;

import com.djelog.entities.ViagemComissionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ViagemComissionadaRepository extends JpaRepository<ViagemComissionada, UUID> {
    List<ViagemComissionada> findByInicioFreteContaining(String inicioFrete);
    
    List<ViagemComissionada> findByFimFreteContaining(String fimFrete);
    
    List<ViagemComissionada> findByValor(Integer valor);
    
    List<ViagemComissionada> findByComissao(Integer comissao);
}
