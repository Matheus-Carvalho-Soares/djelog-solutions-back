package com.djelog.repositories;

import com.djelog.entities.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    List<Empresa> findByUsuario_Id(UUID usuarioId);
    boolean existsByIdAndUsuario_Id(UUID id, UUID usuarioId);
}
