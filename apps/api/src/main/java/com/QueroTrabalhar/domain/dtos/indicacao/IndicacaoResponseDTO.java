package com.QueroTrabalhar.domain.dtos.indicacao;

import com.QueroTrabalhar.domain.entity.Indicacao;
import com.QueroTrabalhar.domain.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IndicacaoResponseDTO", description = "Dados de uma indicação retornados pela API.")
public record IndicacaoResponseDTO(
        @Schema(description = "Identificador da indicação.", example = "14")
        Long id,

        @Schema(description = "Identificador do usuário autor da indicação.", example = "7")
        Long autorId,

        @Schema(description = "Nome do usuário autor da indicação.", example = "Marina Souza")
        String autorNome,

        @Schema(description = "Identificador do usuário indicado.", example = "42")
        Long usuarioIndicadoId,

        @Schema(description = "Nome do usuário indicado.", example = "Carlos Lima")
        String usuarioIndicadoNome,

        @Schema(description = "Mensagem registrada na indicação.", example = "Profissional com excelente domínio de Java e Spring Boot.")
        String mensagem
) {
    public static IndicacaoResponseDTO daEntidade(Indicacao entidade) {
        Usuario autor = entidade.getAutor();
        Usuario usuarioIndicado = entidade.getUsuarioIndicado();

        return new IndicacaoResponseDTO(
                entidade.getId(),
                autor != null ? autor.getId() : null,
                autor != null ? autor.getNome() : null,
                usuarioIndicado != null ? usuarioIndicado.getId() : null,
                usuarioIndicado != null ? usuarioIndicado.getNome() : null,
                entidade.getMensagem()
        );
    }
}
