package com.djelog.repositories;

import com.djelog.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);

    @Query("select u from Usuario u left join fetch u.cargo where u.email = :email")
    Optional<Usuario> findByEmailWithCargo(@Param("email") String email);

    boolean existsByEmail(String email);
}
