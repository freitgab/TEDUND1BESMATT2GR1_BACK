package br.edu.ucsal.clima.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private final JwtService service = new JwtService("a-test-secret-long-enough-for-hmac-signature-12345", 3600000);

    @Test
    void validaTokenAssinadoERejeitaAssinaturaAlterada() {
        String token = service.gerarToken("admin@clima.gov.br");
        assertThat(service.extrairEmail(token)).isEqualTo("admin@clima.gov.br");
        String alterado = token.substring(0, token.lastIndexOf('.') + 1) + "assinatura-adulterada";
        assertThatThrownBy(() -> service.extrairEmail(alterado)).isInstanceOf(JwtException.class);
    }
}
