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

    @Transactional(readOnly = true)
    public java.util.List<LogAuditoria> listarLogs(String acao, String entidadeAfetada, String usuarioEmail) {
        return repository.findAllByOrderByDataHoraDesc().stream()
                .filter(log -> acao == null || acao.isBlank()
                        || (log.getAcao() != null && log.getAcao().toLowerCase().contains(acao.trim().toLowerCase())))
                .filter(log -> entidadeAfetada == null || entidadeAfetada.isBlank()
                        || (log.getEntidadeAfetada() != null && log.getEntidadeAfetada().toLowerCase().contains(entidadeAfetada.trim().toLowerCase())))
                .filter(log -> usuarioEmail == null || usuarioEmail.isBlank()
                        || (log.getUsuarioEmail() != null && log.getUsuarioEmail().toLowerCase().contains(usuarioEmail.trim().toLowerCase())))
                .toList();
    }
}
