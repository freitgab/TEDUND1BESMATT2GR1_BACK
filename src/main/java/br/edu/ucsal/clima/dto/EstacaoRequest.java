package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.TipoEstacao;
import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;
import br.edu.ucsal.clima.domain.enums.TipoSensor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record EstacaoRequest(
        @NotBlank @Size(max = 40) String sigla,
        @NotBlank @Size(max = 160) String nome,
        @NotNull TipoEstacao tipoEstacao,
        @Size(max = 1000) String descricaoComplementar,
        @NotNull @Positive Integer capacidadeMaximaSuportada,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull Long redeMonitoramentoId,
        List<TipoSensor> sensores,
        List<@Valid SoftwareDTO> softwares,
        String nomeSoftware,
        String urlDownload,
        TipoLicencaSoftware tipoSoftware,
        String objetivoUso,
        Set<String> variaveisMonitoradas
) {
    public EstacaoRequest(
            String sigla,
            String nome,
            TipoEstacao tipoEstacao,
            String descricaoComplementar,
            Integer capacidadeMaximaSuportada,
            Double latitude,
            Double longitude,
            Long redeMonitoramentoId,
            List<TipoSensor> sensores,
            List<SoftwareDTO> softwares
    ) {
        this(sigla, nome, tipoEstacao, descricaoComplementar, capacidadeMaximaSuportada,
                latitude, longitude, redeMonitoramentoId, sensores, softwares,
                null, null, null, null, null);
    }
}
