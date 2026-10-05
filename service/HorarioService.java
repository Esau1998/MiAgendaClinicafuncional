package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Horario;
import org.esfe.miagendaclinica.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    public List<Horario> listar() {
        return horarioRepository.findAll();
    }

    public List<Horario> listarPorMedico(Long medicoId) {
        return horarioRepository.findByMedicoId(medicoId);
    }

    public Optional<Horario> buscarPorId(Long id) {
        return horarioRepository.findById(id);
    }

    public Horario guardar(Horario horario) {
        return horarioRepository.save(horario);
    }

    public void eliminar(Long id) {
        horarioRepository.deleteById(id);
    }
}
