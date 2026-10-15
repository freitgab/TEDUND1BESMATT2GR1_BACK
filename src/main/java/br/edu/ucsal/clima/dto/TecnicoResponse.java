package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import java.time.LocalDate;

public record TecnicoResponse(Long id, String matricula, String nomeCompleto, String email,
                              String telefone, Long usuarioId, Long distritoId, String distritoNome,
                              LocalDate dataCadastro, StatusEntidade status, String motivoInativacao) { }
