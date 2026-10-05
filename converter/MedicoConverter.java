package org.esfe.miagendaclinica.converter;

import org.esfe.miagendaclinica.model.Medico;
import org.esfe.miagendaclinica.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MedicoConverter implements Converter<String, Medico> {

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public Medico convert(String source) {
        if (source == null || source.isBlank()) return null;
        return medicoRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
