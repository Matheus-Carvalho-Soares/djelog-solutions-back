package com.djelog.repositories;

import com.djelog.entities.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {
    List<Profissional> findByUsuario_Id(UUID usuarioId);

    boolean existsByIdAndUsuario_Id(UUID id, UUID usuarioId);
}
