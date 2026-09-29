package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.infrastructure.client.google.dto.AddressComponent;
import com.QueroTrabalhar.infrastructure.client.google.dto.GoogleResult;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Converte um resultado único do Google Maps em uma {@link Localidade} coerente com o catálogo interno.
 *
 * <p>Esta classe existe para impedir que a integração externa atravesse o domínio sem validação.
 * O resultado só é promovido quando a hierarquia país/estado/cidade vem consistente. Quando isso
 * acontece, a conversão reaproveita ou cria registros internos para que a localidade validada
 * continue sendo a fonte oficial persistida no banco.</p>
 */
@Service
public class ConversorGoogleResultParaLocalidade {

    private final PaisRepository paisRepository;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;

    public ConversorGoogleResultParaLocalidade(
            PaisRepository paisRepository,
            EstadoRepository estadoRepository,
            CidadeRepository cidadeRepository
    ) {
        this.paisRepository = paisRepository;
        this.estadoRepository = estadoRepository;
        this.cidadeRepository = cidadeRepository;
    }

    /**
     * Converte o retorno do Google para a representação oficial do domínio ou rejeita o resultado inconsistente.
     */
    public ResultadoConversaoGoogleParaLocalidade converter(GoogleResult resultadoGoogle) {
        // Google Maps é apenas fallback de validação. Só promovemos o resultado quando a hierarquia vier consistente.
        AddressComponent componentePais = extrairComponente(resultadoGoogle, "country");
        if (!componenteValido(componentePais)) {
            return ResultadoConversaoGoogleParaLocalidade.invalida("PAIS_AUSENTE");
        }
        if (!textoPreenchido(componentePais.shortName())) {
            return ResultadoConversaoGoogleParaLocalidade.invalida("SIGLA_PAIS_AUSENTE");
        }

        Pais pais = obterOuCriarPais(componentePais);
        AddressComponent componenteEstado = extrairComponente(resultadoGoogle, "administrative_area_level_1");
        AddressComponent componenteCidade = extrairComponente(
                resultadoGoogle,
                "locality",
                "administrative_area_level_2"
        );

        if (componenteCidade != null && !componenteValido(componenteEstado)) {
            return ResultadoConversaoGoogleParaLocalidade.invalida("CIDADE_SEM_ESTADO");
        }

        if (!componenteValido(componenteEstado)) {
            return ResultadoConversaoGoogleParaLocalidade.resolvida(new Localidade(pais));
        }

        Estado estado = obterOuCriarEstado(componenteEstado, pais);
        if (!componenteValido(componenteCidade)) {
            return ResultadoConversaoGoogleParaLocalidade.resolvida(new Localidade(pais, estado));
        }

        Cidade cidade = obterOuCriarCidade(componenteCidade, estado);
        return ResultadoConversaoGoogleParaLocalidade.resolvida(new Localidade(pais, estado, cidade));
    }

    private Pais obterOuCriarPais(AddressComponent componentePais) {
        String nomePais = componentePais.longName().trim();
        String siglaPais = componentePais.shortName().trim().toUpperCase();

        return paisRepository.findFirstBySiglaIgnoreCase(siglaPais)
                .or(() -> paisRepository.findFirstByNomeIgnoreCase(nomePais))
                .orElseGet(() -> paisRepository.save(new Pais(nomePais, siglaPais)));
    }

    private Estado obterOuCriarEstado(AddressComponent componenteEstado, Pais pais) {
        String nomeEstado = componenteEstado.longName().trim();
        String siglaEstado = normalizarTextoOpcional(componenteEstado.shortName());

        return estadoRepository.findFirstByNomeIgnoreCaseAndPais(nomeEstado, pais)
                .orElseGet(() -> estadoRepository.save(new Estado(nomeEstado, siglaEstado, pais)));
    }

    private Cidade obterOuCriarCidade(AddressComponent componenteCidade, Estado estado) {
        String nomeCidade = componenteCidade.longName().trim();

        return cidadeRepository.findFirstByNomeIgnoreCaseAndEstado(nomeCidade, estado)
                .orElseGet(() -> cidadeRepository.save(new Cidade(nomeCidade, estado)));
    }

    private AddressComponent extrairComponente(GoogleResult resultado, String... tiposDesejados) {
        List<String> tiposAlvo = List.of(tiposDesejados);
        if (resultado.addressComponents() == null) {
            return null;
        }

        for (AddressComponent componente : resultado.addressComponents()) {
            if (componente.types() == null) {
                continue;
            }

            for (String tipo : componente.types()) {
                if (tiposAlvo.contains(tipo)) {
                    return componente;
                }
            }
        }

        return null;
    }

    private boolean componenteValido(AddressComponent componente) {
        return componente != null && textoPreenchido(componente.longName());
    }

    private boolean textoPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isBlank() ? null : valorNormalizado;
    }
}
