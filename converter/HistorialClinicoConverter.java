package org.esfe.miagendaclinica.converter;

import org.esfe.miagendaclinica.model.HistorialClinico;
import org.esfe.miagendaclinica.repository.HistorialClinicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class HistorialClinicoConverter implements Converter<String, HistorialClinico> {

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Override
    public HistorialClinico convert(String source) {
        if (source == null || source.isBlank()) return null;
        return historialClinicoRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
