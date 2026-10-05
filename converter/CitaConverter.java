package org.esfe.miagendaclinica.converter;

import org.esfe.miagendaclinica.model.Cita;
import org.esfe.miagendaclinica.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CitaConverter implements Converter<String, Cita> {

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public Cita convert(String source) {
        if (source == null || source.isBlank()) return null;
        return citaRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
