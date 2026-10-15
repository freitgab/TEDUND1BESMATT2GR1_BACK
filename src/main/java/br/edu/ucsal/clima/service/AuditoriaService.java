package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.LogAuditoria;
import br.edu.ucsal.clima.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final LogAuditoriaRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String acao, String entidade, Long id, String detalhes) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())
                ? authentication.getName() : "sistema";
        var log = new LogAuditoria();
        log.setUsuarioEmail(email);
        log.setAcao(acao);
        log.setEntidadeAfetada(entidade);
        log.setDetalhes("id=" + id + (detalhes == null || detalhes.isBlank() ? "" : "; " + detalhes));
        repository.save(log);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String acao, String entidade, String detalhes) {
        registrar(acao, entidade, null, detalhes);
    }
}
