package br.edu.ucsal.clima.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DmcRequest(@NotBlank @Size(max = 160) String nome,
                         @Email @Size(max = 160) String emailCentral,
                         @Size(max = 30) String telefoneCentral,
                         @NotNull Long orgaoGestorId,
                         Long meteorologistaChefeId) { }
