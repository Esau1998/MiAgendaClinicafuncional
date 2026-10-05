package org.esfe.miagendaclinica.converter;

import org.esfe.miagendaclinica.model.Rol;
import org.esfe.miagendaclinica.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RolConverter implements Converter<String, Rol> {

    @Autowired
    private RolRepository rolRepository;

    @Override
    public Rol convert(String source) {
        if (source == null || source.isBlank()) return null;
        return rolRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
