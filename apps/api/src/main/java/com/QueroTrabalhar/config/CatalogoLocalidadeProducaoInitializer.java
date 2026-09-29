package com.QueroTrabalhar.config;

import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Garante um catálogo mínimo e idempotente para os primeiros cadastros em produção. */
@Configuration
@Profile("prod")
@ConditionalOnProperty(
        name = "app.bootstrap-localidades.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class CatalogoLocalidadeProducaoInitializer implements ApplicationRunner {

    private static final List<String> CIDADES_INICIAIS = List.of(
            "Salvador",
            "Feira de Santana",
            "Camaçari",
            "Lauro de Freitas"
    );

    private final PaisRepository paisRepository;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;

    public CatalogoLocalidadeProducaoInitializer(
            PaisRepository paisRepository,
            EstadoRepository estadoRepository,
            CidadeRepository cidadeRepository
    ) {
        this.paisRepository = paisRepository;
        this.estadoRepository = estadoRepository;
        this.cidadeRepository = cidadeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Pais brasil = paisRepository.findFirstBySiglaIgnoreCase("BR")
                .orElseGet(() -> paisRepository.save(new Pais("Brasil", "BR")));

        Estado bahia = estadoRepository.findFirstByNomeIgnoreCaseAndPais("Bahia", brasil)
                .orElseGet(() -> estadoRepository.save(new Estado("Bahia", "BA", brasil)));

        for (String nomeCidade : CIDADES_INICIAIS) {
            cidadeRepository.findFirstByNomeIgnoreCaseAndEstado(nomeCidade, bahia)
                    .orElseGet(() -> cidadeRepository.save(new Cidade(nomeCidade, bahia)));
        }
    }
}
