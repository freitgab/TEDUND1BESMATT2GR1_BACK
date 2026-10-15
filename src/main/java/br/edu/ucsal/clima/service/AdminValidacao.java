package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import java.util.Locale;

final class AdminValidacao {
    private AdminValidacao() { }

    static Long idObrigatorio(Long id, String campo) {
        if (id == null || id <= 0) throw new IllegalArgumentException(campo + " deve informar um ID valido");
        return id;
    }

    static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " e obrigatorio");
        return valor.trim();
    }

    static String email(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toLowerCase(Locale.ROOT);
    }

    static void exigirAtivo(StatusEntidade status, String entidade) {
        if (status != StatusEntidade.ATIVO) throw new IllegalStateException(entidade + " esta inativo");
    }

    static void requireAtivo(StatusEntidade status, String entidade) {
        exigirAtivo(status, entidade);
    }
}
