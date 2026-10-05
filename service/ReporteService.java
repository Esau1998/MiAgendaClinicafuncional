package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Cita;
import org.esfe.miagendaclinica.model.Pago;
import org.esfe.miagendaclinica.repository.CitaRepository;
import org.esfe.miagendaclinica.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    public List<Cita> reporteCitas() {
        return citaRepository.findAll();
    }

    public Map<String, Long> reportePorMedico() {
        return citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getMedico().getNombre() + " " + c.getMedico().getApellido(),
                        Collectors.counting()));
    }

    public Map<String, Long> reportePorEspecialidad() {
        return citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getEspecialidad().getNombre(),
                        Collectors.counting()));
    }

    public List<Pago> reportePagos() {
        return pagoRepository.findAll();
    }
}
