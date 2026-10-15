package br.edu.ucsal.clima.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(LocalDateTime timestamp, int status, String erro, String mensagem,
                       Map<String, String> campos) { }
