package org.esfe.miagendaclinica.converter;

import org.esfe.miagendaclinica.model.Paciente;
import org.esfe.miagendaclinica.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PacienteConverter implements Converter<String, Paciente> {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Override
    public Paciente convert(String source) {
        if (source == null || source.isBlank()) return null;
        return pacienteRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
