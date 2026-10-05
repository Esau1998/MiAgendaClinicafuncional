package org.esfe.miagendaclinica.converter;

import org.esfe.miagendaclinica.model.Especialidad;
import org.esfe.miagendaclinica.repository.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EspecialidadConverter implements Converter<String, Especialidad> {

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Override
    public Especialidad convert(String source) {
        if (source == null || source.isBlank()) return null;
        return especialidadRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
