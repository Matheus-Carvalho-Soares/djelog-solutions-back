package com.djelog.services;

import com.djelog.entities.Empresa;
import com.djelog.repositories.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public boolean delete(UUID empresaId, UUID usuarioId) {
        Empresa empresa = empresaRepository.findByIdAndUsuario_Id(empresaId, usuarioId).orElse(null);
        if (empresa == null) return false;
        empresaRepository.delete(empresa);
        return true;
    }
}
