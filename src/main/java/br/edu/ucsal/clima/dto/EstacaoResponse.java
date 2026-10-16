package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.domain.enums.TipoEstacao;
import br.edu.ucsal.clima.domain.enums.TipoSensor;

import java.time.LocalDate;
import java.util.List;

public record EstacaoResponse(
        Long id,
        String sigla,
        String nome,
        TipoEstacao tipoEstacao,
        String descricaoComplementar,
        Integer capacidadeMaximaSuportada,
        Double latitude,
        Double longitude,
        Long redeMonitoramentoId,
        String redeMonitoramentoSigla,
        List<TipoSensor> sensores,
        List<SoftwareDTO> softwares,
        LocalDate dataCadastro,
        StatusEstacao status
) { }
