package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.DistritoMonitoramentoClimatico;
import br.edu.ucsal.clima.domain.entity.OrgaoGestorMeteorologia;
import br.edu.ucsal.clima.domain.entity.TecnicoMeteorologista;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.dto.DmcRequest;
import br.edu.ucsal.clima.dto.DmcResponse;
import br.edu.ucsal.clima.repository.DistritoMonitoramentoClimaticoRepository;
import br.edu.ucsal.clima.repository.OrgaoGestorMeteorologiaRepository;
import br.edu.ucsal.clima.repository.RedeMonitoramentoRepository;
import br.edu.ucsal.clima.repository.TecnicoMeteorologistaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistritoMonitoramentoClimaticoService {

    private final DistritoMonitoramentoClimaticoRepository dmcRepository;
    private final OrgaoGestorMeteorologiaRepository orgaoRepository;
    private final TecnicoMeteorologistaRepository tecnicoRepository;
    private final RedeMonitoramentoRepository redeRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<DmcResponse> listarTodos() {
        return dmcRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DmcResponse buscarPorId(Long id) {
        return dmcRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado com o ID informado"));
    }

    @Transactional
    public DmcResponse cadastrar(DmcRequest req) {
        String nome = AdminValidation.textoObrigatorio(req.nome(), "Nome");
        Long orgaoId = AdminValidation.idObrigatorio(req.orgaoGestorId(), "Orgao Gestor");

        if (dmcRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Nome ja cadastrado para outro DMC");
        }

        OrgaoGestorMeteorologia ogm = orgaoRepository.findById(orgaoId)
                .orElseThrow(() -> new EntityNotFoundException("OGM nao encontrado"));
        AdminValidation.requireAtivo(ogm.getStatus(), "OGM");

        TecnicoMeteorologista chefe = null;
        if (req.meteorologistaChefeId() != null) {
            chefe = tecnicoRepository.findById(req.meteorologistaChefeId())
                    .orElseThrow(() -> new EntityNotFoundException("Meteorologista chefe nao encontrado"));
            AdminValidation.requireAtivo(chefe.getStatus(), "Meteorologista chefe");

            if (dmcRepository.existsByMeteorologistaChefe_Id(chefe.getId())) {
                throw new IllegalStateException("Tecnico ja e meteorologista chefe de outro DMC");
            }
        }

        var dmc = new DistritoMonitoramentoClimatico();
        dmc.setNome(nome);
        dmc.setEmailCentral(AdminValidation.email(req.emailCentral()));
        dmc.setTelefoneCentral(req.telefoneCentral() != null ? req.telefoneCentral().trim() : null);
        dmc.setOrgaoGestor(ogm);
        dmc.setMeteorologistaChefe(chefe);
        dmc.setStatus(StatusEntidade.ATIVO);

        dmc = dmcRepository.save(dmc);
        auditoriaService.registrar("CADASTRAR", "DistritoMonitoramentoClimatico", dmc.getId(), "nome=" + dmc.getNome());
        return toResponse(dmc);
    }

    @Transactional
    public DmcResponse atualizar(Long id, DmcRequest req) {
        var dmc = dmcRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado com o ID informado"));

        String nome = AdminValidation.textoObrigatorio(req.nome(), "Nome");
        Long orgaoId = AdminValidation.idObrigatorio(req.orgaoGestorId(), "Orgao Gestor");

        if (dmcRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new IllegalStateException("Nome ja cadastrado para outro DMC");
        }

        OrgaoGestorMeteorologia ogm = orgaoRepository.findById(orgaoId)
                .orElseThrow(() -> new EntityNotFoundException("OGM nao encontrado"));
        AdminValidation.requireAtivo(ogm.getStatus(), "OGM");

        TecnicoMeteorologista chefe = null;
        if (req.meteorologistaChefeId() != null) {
            chefe = tecnicoRepository.findById(req.meteorologistaChefeId())
                    .orElseThrow(() -> new EntityNotFoundException("Meteorologista chefe nao encontrado"));
            AdminValidation.requireAtivo(chefe.getStatus(), "Meteorologista chefe");

            if (dmcRepository.existsByMeteorologistaChefe_IdAndIdNot(chefe.getId(), id)) {
                throw new IllegalStateException("Tecnico ja e meteorologista chefe de outro DMC");
            }
        }

        dmc.setNome(nome);
        dmc.setEmailCentral(AdminValidation.email(req.emailCentral()));
        dmc.setTelefoneCentral(req.telefoneCentral() != null ? req.telefoneCentral().trim() : null);
        dmc.setOrgaoGestor(ogm);
        dmc.setMeteorologistaChefe(chefe);

        dmc = dmcRepository.save(dmc);
        auditoriaService.registrar("ATUALIZAR", "DistritoMonitoramentoClimatico", dmc.getId(), "nome=" + dmc.getNome());
        return toResponse(dmc);
    }

    @Transactional
    public void inativar(Long id) {
        var dmc = dmcRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado com o ID informado"));

        if (dmc.getStatus() == StatusEntidade.INATIVO) {
            throw new IllegalStateException("DMC ja esta inativo");
        }

        if (redeRepository.existsByDistrito_IdAndStatus(id, StatusEntidade.ATIVO)) {
            throw new IllegalStateException("Nao e possivel inativar DMC com Redes de Monitoramento ativas vinculadas");
        }

        if (tecnicoRepository.existsByDistrito_IdAndStatus(id, StatusEntidade.ATIVO)) {
            throw new IllegalStateException("Nao e possivel inativar DMC com Tecnicos Meteorologistas ativos vinculados");
        }

        dmc.setStatus(StatusEntidade.INATIVO);
        dmcRepository.save(dmc);
        auditoriaService.registrar("INATIVAR", "DistritoMonitoramentoClimatico", dmc.getId(), "nome=" + dmc.getNome());
    }

    private DmcResponse toResponse(DistritoMonitoramentoClimatico dmc) {
        Long orgaoId = dmc.getOrgaoGestor() != null ? dmc.getOrgaoGestor().getId() : null;
        String orgaoNome = dmc.getOrgaoGestor() != null ? dmc.getOrgaoGestor().getNome() : null;
        Long chefeId = dmc.getMeteorologistaChefe() != null ? dmc.getMeteorologistaChefe().getId() : null;
        String chefeNome = dmc.getMeteorologistaChefe() != null ? dmc.getMeteorologistaChefe().getNomeCompleto() : null;

        return new DmcResponse(
                dmc.getId(),
                dmc.getNome(),
                dmc.getEmailCentral(),
                dmc.getTelefoneCentral(),
                orgaoId,
                orgaoNome,
                chefeId,
                chefeNome,
                dmc.getDataCadastro(),
                dmc.getStatus()
        );
    }
}
