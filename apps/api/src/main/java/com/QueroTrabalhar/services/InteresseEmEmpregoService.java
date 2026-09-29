package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.entity.Preferencia;
import com.QueroTrabalhar.repository.InteresseEmEmpregoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteresseEmEmpregoService {

    @Autowired
    private InteresseEmEmpregoRepository interesseEmEmpregoRepository;

    public List<Preferencia> ListarInteressesEmEmpregos() {
        return interesseEmEmpregoRepository.findAll();
    }

    public Preferencia salvarInteresseEmEmprego(Preferencia preferencia) {
        return interesseEmEmpregoRepository.save(preferencia);
    }

    public void deletarInteresseEmEmprego(Long id) {
        interesseEmEmpregoRepository.deleteById(id);
    }
}
