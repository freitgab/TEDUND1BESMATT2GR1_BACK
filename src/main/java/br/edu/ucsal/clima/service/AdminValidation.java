package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;

public final class AdminValidation {
    private AdminValidation() { }

    public static Long idObrigatorio(Long id, String campo) {
        return AdminValidacao.idObrigatorio(id, campo);
    }

    public static String textoObrigatorio(String valor, String campo) {
        return AdminValidacao.textoObrigatorio(valor, campo);
    }

    public static String email(String valor) {
        return AdminValidacao.email(valor);
    }

    public static void exigirAtivo(StatusEntidade status, String entidade) {
        AdminValidacao.exigirAtivo(status, entidade);
    }

    public static void requireAtivo(StatusEntidade status, String entidade) {
        AdminValidacao.requireAtivo(status, entidade);
    }
}
