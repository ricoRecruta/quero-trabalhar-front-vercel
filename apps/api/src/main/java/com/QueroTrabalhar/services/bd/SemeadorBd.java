package com.QueroTrabalhar.services.bd;

import com.QueroTrabalhar.domain.entity.*;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.*;
import com.QueroTrabalhar.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SemeadorBd {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TipoDeEmpregoRepository tipoDeEmpregoRepository;
    @Autowired private OportunidadeDeEmpregoRepository oportunidadeRepository;
    @Autowired private IndicacaoRepository indicacaoRepository;
    @Autowired private CidadeRepository cidadeRepository;
    @Autowired private EstadoRepository estadoRepository;
    @Autowired private PaisRepository paisRepository;
    @Autowired private BCryptPasswordEncoder encoder;

    @Autowired private ObjectMapper objectMapper;
    //pequeno record para popular o banco
    record PaisJsonDto(String nome, String sigla){}

    @Transactional
    public void popularBd() {

        limparBd();
        // =========================================================
        // ONDA 0 - LOCALIDADE (PAISES, ESTADOS E CIDADES)
        // =========================================================

        System.out.println("Semeando Países do arquivo JSON...");

        try {
            // Lê o arquivo da pasta resources
            ClassPathResource resource = new ClassPathResource("paises.json");

            // Converte o JSON para uma Lista do nosso DTO temporário
            List<PaisJsonDto> paisesJson = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );

            // Transforma os DTOs em Entidades reais do Hibernate
            List<Pais> paisesParaSalvar = paisesJson.stream()
                    .map(dto -> new Pais(dto.nome(), dto.sigla()))
                    .toList();

            // Salva todos os 195 de uma vez só! (Super rápido)
            paisRepository.saveAll(paisesParaSalvar);

        } catch (Exception e) {
            System.err.println("Erro ao carregar o JSON de Países: " + e.getMessage());
        }

        Pais brasil = paisRepository.findBySigla("BR").orElseThrow(() -> new RuntimeException("Brasil sumiu do JSON!"));

        Pais eua = paisRepository.findBySigla("US").orElseThrow(() -> new RuntimeException("US sumiu do JSON!"));

        Estado paraiba = estadoRepository.save(new Estado("Paraíba","PB", brasil));
        Cidade joaoPessoa = cidadeRepository.save(new Cidade("João Pessoa", paraiba));
        Cidade rioTinto = cidadeRepository.save(new Cidade("Rio Tinto", paraiba));

        // =========================================================
        // ONDA 1 - TIPOS DE EMPREGO
        // =========================================================

        TipoDeEmprego ti = TipoDeEmprego.criarTipoDeEmpregoAdmin("Tecnologia", "Desenvolvimento e Infraestrutura");
        TipoDeEmprego design = TipoDeEmprego.criarTipoDeEmpregoAdmin("Design", "UX/UI e Produto");
        TipoDeEmprego manutencao = TipoDeEmprego.criarTipoDeEmpregoAdmin("Manutenção", "Serviços técnicos");
        TipoDeEmprego jardineiro = TipoDeEmprego.criarTipoDeEmpregoAdmin("Jardineiro(a)", "Podar árvores");
        TipoDeEmprego eletricista = TipoDeEmprego.criarTipoDeEmpregoAdmin("Eletricista", "Instalações elétricas");

        TipoDeEmprego tipoUsario = TipoDeEmprego.criarTipoDeEmpregoSugeridoPeloUsuario("Atendente de caixa","Trabalhar em supermercado");

        tipoDeEmpregoRepository.saveAll(List.of(ti, design, manutencao, jardineiro, eletricista,tipoUsario));

        // =========================================================
        // ONDA 2 - USUÁRIOS
        // =========================================================

        Usuario admin = new Usuario("27128833064", "Admin Master", "83999990000",
                "admin@gmail.com", encoder.encode("123456"));
        admin.addProfile(Role.ADMIN);

        Usuario candidato1 = new Usuario("63069141021", "Dan Pink", "83999990001",
                "dan@gmail.com", encoder.encode("123456"));

        Usuario candidato2 = new Usuario("75772202057", "Emma Black", "83999990002",
                "emma@gmail.com", encoder.encode("123456"));

        Usuario recrutadorUser = new Usuario("81004206020", "Bob Red", "83988887777",
                "rh@tech.com", encoder.encode("123456"));

        Usuario usuarioDuplo  = new Usuario("63700679033", "Jane LightPink", "83998765432", "jane@gmail.com", encoder.encode("123456"));

        // =========================================================
        // ONDA 3 - PERFIS
        // =========================================================

        // Candidatos
        PerfilCandidato perfilDan = new PerfilCandidato(candidato1);
        PerfilCandidato perfilEmma = new PerfilCandidato(candidato2);

        candidato1.adicionarPerfilCandidato(perfilDan);
        candidato2.adicionarPerfilCandidato(perfilEmma);

        // Recrutador
        PerfilRecrutador perfilAdmin = new PerfilRecrutador(admin,"Admin Corp");
        admin.adicionarPerfilRecrutador(perfilAdmin);
        PerfilRecrutador perfilRH = new PerfilRecrutador(recrutadorUser, "Tech Solutions");
        recrutadorUser.adicionarPerfilRecrutador(perfilRH);

        //Perfis duplo
        PerfilRecrutador perfilRecrutadorDuplo = new PerfilRecrutador(usuarioDuplo, "Autonomo");
        usuarioDuplo.adicionarPerfilRecrutador(perfilRecrutadorDuplo);
        PerfilCandidato perfilCandidatoDuplo = new PerfilCandidato(usuarioDuplo);
        usuarioDuplo.adicionarPerfilCandidato(perfilCandidatoDuplo);

        usuarioRepository.saveAll(List.of(admin, candidato1, candidato2, recrutadorUser, usuarioDuplo));
        usuarioRepository.flush(); // força geração de IDs

        // =========================================================
        // ONDA 4 - EXPERIÊNCIAS PROFISSIONAIS
        // =========================================================

        ExperienciaProfissional exp1 = new ExperienciaProfissional(
                perfilDan,
                ti,
                "Desenvolvedor Java Backend",
                LocalDate.of(2022, 1, 10),
                LocalDate.of(2024, 1, 10)
        );

        perfilDan.adicionarExperiencia(exp1);

        ExperienciaProfissional expJardineiro = new ExperienciaProfissional(
                perfilCandidatoDuplo,
                jardineiro,
                "Trabalhei podando arvores para a prefeitura",
                LocalDate.of(2022, 1, 10),
                LocalDate.of(2024, 1, 10)
        );
        perfilCandidatoDuplo.adicionarExperiencia(expJardineiro);

        // =========================================================
        // ONDA 5 - INTERESSE EM EMPREGO
        // =========================================================

        Preferencia interesseDan = new Preferencia(perfilDan, true);
        interesseDan.adicionarTipoInteresse(ti);
        interesseDan.adicionarTipoInteresse(design);


        interesseDan.adicionarLocal(new Localidade(brasil, paraiba, joaoPessoa));

        interesseDan.adicionarLocal(new Localidade(brasil, paraiba, rioTinto));

        interesseDan.adicionarLocal(new Localidade(eua));

        perfilDan.definirInteresse(interesseDan);

        Preferencia interesseCandidatoDuplo = new Preferencia(perfilCandidatoDuplo, true);
        interesseCandidatoDuplo.adicionarTipoInteresse(jardineiro);


        interesseCandidatoDuplo.adicionarLocal(new Localidade(brasil,paraiba,rioTinto));

        perfilCandidatoDuplo.definirInteresse(interesseCandidatoDuplo);

        // =========================================================
        // ONDA 6 - OPORTUNIDADES
        // =========================================================

        Localidade loc = new Localidade(brasil, paraiba, joaoPessoa);

        OportunidadeDeEmprego vaga1 = new OportunidadeDeEmprego(
                "Dev Spring Boot",
                ti,
                Modalidade.REMOTO,
                loc,
                perfilRH
        );

        OportunidadeDeEmprego vaga2 = new OportunidadeDeEmprego(
                "UX Designer Pleno",
                design,
                Modalidade.HIBRIDO,
                loc,
                perfilRH
        );

        OportunidadeDeEmprego vaga3 = new OportunidadeDeEmprego(
                "Eletrotécnico",
                eletricista,
                Modalidade.PRESENCIAL,
                new Localidade(brasil, paraiba, rioTinto),
                perfilRecrutadorDuplo
        );

        perfilRH.adicionarOportunidadePostada(vaga1);
        perfilRH.adicionarOportunidadePostada(vaga2);
        perfilRecrutadorDuplo.adicionarOportunidadePostada(vaga3);

        oportunidadeRepository.saveAll(List.of(vaga1, vaga2, vaga3));

        // =========================================================
        // ONDA 7 - INTERESSE EM VAGAS (ManyToMany)
        // =========================================================

        perfilDan.demonstrarInteresse(vaga1);
        perfilEmma.demonstrarInteresse(vaga2);
        perfilDan.demonstrarInteresse(vaga3);

        // =========================================================
        // ONDA 8 - INDICAÇÕES
        // =========================================================

        Indicacao indicacao = new Indicacao(
                candidato2,
                candidato1,
                "Excelente profissional backend!"
        );

        Indicacao indicacao1 = new Indicacao(
                perfilRH.getUsuario(),
                perfilCandidatoDuplo.getUsuario(),
                "Ótimo Jardineiro"
        );

        candidato2.adicionarIndicacaoDada(indicacao);
        candidato1.adicionarIndicacaoRecebida(indicacao);

        admin.adicionarIndicacaoDada(indicacao1);
        usuarioDuplo.adicionarIndicacaoRecebida(indicacao1);

        usuarioRepository.saveAll(List.of(candidato1, candidato2,admin, usuarioDuplo));

        System.out.println(">>> BANCO POPULADO COM SUCESSO <<<");
    }

    private void limparBd() {
        indicacaoRepository.deleteAllInBatch();
        oportunidadeRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();
        tipoDeEmpregoRepository.deleteAllInBatch();
    }
}