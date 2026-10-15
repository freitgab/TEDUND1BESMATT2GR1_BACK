package br.edu.ucsal.clima.auth.dto;

import br.edu.ucsal.clima.domain.enums.NomePerfil;
import java.util.List;

public record LoginResponse(String token, String nome, String email, List<NomePerfil> perfis) { }
