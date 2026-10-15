package br.edu.ucsal.clima.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TecnicoEdicaoRequest(@NotBlank @Size(max = 40) String matricula,
                                   @NotBlank @Size(max = 160) String nomeCompleto,
                                   @NotBlank @Email @Size(max = 160) String email,
                                   @Size(max = 30) String telefone,
                                   @NotNull Long distritoId) { }
