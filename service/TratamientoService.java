package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Tratamiento;
import org.esfe.miagendaclinica.repository.TratamientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TratamientoService {

    @Autowired
    private TratamientoRepository tratamientoRepository;

    public List<Tratamiento> listar() {
        return tratamientoRepository.findAll();
    }

    public List<Tratamiento> listarPorHistorial(Long historialId) {
        return tratamientoRepository.findByHistorialClinicoId(historialId);
    }

    public Optional<Tratamiento> buscarPorId(Long id) {
        return tratamientoRepository.findById(id);
    }

    public Tratamiento guardar(Tratamiento tratamiento) {
        return tratamientoRepository.save(tratamiento);
    }

    public void eliminar(Long id) {
        tratamientoRepository.deleteById(id);
    }
}
