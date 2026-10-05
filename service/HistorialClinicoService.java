package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.HistorialClinico;
import org.esfe.miagendaclinica.repository.HistorialClinicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialClinicoService {

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    public List<HistorialClinico> listarPorPaciente(Long pacienteId) {
        return historialClinicoRepository.findByPacienteIdOrderByFechaDesc(pacienteId);
    }

    public List<HistorialClinico> listar() {
        return historialClinicoRepository.findAll();
    }

    public Optional<HistorialClinico> buscarPorId(Long id) {
        return historialClinicoRepository.findById(id);
    }

    public HistorialClinico guardar(HistorialClinico historialClinico) {
        return historialClinicoRepository.save(historialClinico);
    }
}
