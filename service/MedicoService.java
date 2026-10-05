package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Medico;
import org.esfe.miagendaclinica.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    public List<Medico> listar() {
        return medicoRepository.findAll();
    }

    public List<Medico> listarActivos() {
        return medicoRepository.findByEstado("ACTIVO");
    }

    public List<Medico> buscar(String texto) {
        return medicoRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(texto, texto);
    }

    public Optional<Medico> buscarPorId(Long id) {
        return medicoRepository.findById(id);
    }

    public Medico guardar(Medico medico) {
        return medicoRepository.save(medico);
    }

    public void desactivar(Long id) {
        medicoRepository.findById(id).ifPresent(m -> {
            m.setEstado("INACTIVO");
            medicoRepository.save(m);
        });
    }

    public long contarActivos() {
        return medicoRepository.findByEstado("ACTIVO").size();
    }
}
