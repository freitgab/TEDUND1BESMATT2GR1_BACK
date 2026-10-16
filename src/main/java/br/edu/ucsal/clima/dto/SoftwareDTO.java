package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SoftwareDTO(
        Long id,
        @NotBlank @Size(max = 160) String nome,
        @NotBlank @Size(max = 1000) String urlDownload,
        @NotNull TipoLicencaSoftware tipoSoftware,
        @NotBlank @Size(max = 1000) String objetivoUso,
        Set<String> variaveisMonitoradas
) { }
