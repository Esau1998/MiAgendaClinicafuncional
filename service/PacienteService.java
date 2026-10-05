package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Paciente;
import org.esfe.miagendaclinica.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    public List<Paciente> listar() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> listarActivos() {
        return pacienteRepository.findByEstado("ACTIVO");
    }

    public List<Paciente> buscar(String texto) {
        return pacienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(texto, texto);
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }

    public Paciente guardar(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public void desactivar(Long id) {
        pacienteRepository.findById(id).ifPresent(p -> {
            p.setEstado("INACTIVO");
            pacienteRepository.save(p);
        });
    }

    public long contarActivos() {
        return pacienteRepository.findByEstado("ACTIVO").size();
    }
}
