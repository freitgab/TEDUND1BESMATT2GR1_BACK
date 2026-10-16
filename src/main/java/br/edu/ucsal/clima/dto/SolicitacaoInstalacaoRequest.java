package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoInstalacaoRequest(
        @NotNull Long estacaoId,
        @NotBlank @Size(max = 1000) String sensoresSoftwares,
        @Size(max = 1000) String urlDownload,
        TipoLicencaSoftware tipoSoftware,
        @NotBlank @Size(max = 1000) String objetivoUso
) { }
