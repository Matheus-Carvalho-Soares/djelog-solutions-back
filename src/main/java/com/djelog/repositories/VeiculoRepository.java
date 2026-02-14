package com.djelog.repositories;

import com.djelog.entities.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {
    List<Veiculo> findByProfissional_Usuario_Id(UUID usuarioId);
    boolean existsByIdAndProfissional_Usuario_Id(UUID id, UUID usuarioId);
}
