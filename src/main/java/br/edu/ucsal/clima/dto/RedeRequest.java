package br.edu.ucsal.clima.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RedeRequest(@NotBlank @Size(max = 40) String sigla,
                          @NotBlank @Size(max = 500) String descricao,
                          @NotNull Long distritoId,
                          @NotEmpty Set<@NotBlank String> tiposMonitoramento,
                          Long meteorologistaResponsavelId) { }
