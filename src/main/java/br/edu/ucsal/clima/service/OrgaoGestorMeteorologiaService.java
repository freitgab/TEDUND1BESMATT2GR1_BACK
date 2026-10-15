package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.OrgaoGestorMeteorologia;
import br.edu.ucsal.clima.domain.entity.SetorCoordenadoria;
import br.edu.ucsal.clima.domain.entity.Usuario;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.dto.OgmRequest;
import br.edu.ucsal.clima.dto.OgmResponse;
import br.edu.ucsal.clima.dto.SetorDTO;
import br.edu.ucsal.clima.repository.DistritoMonitoramentoClimaticoRepository;
import br.edu.ucsal.clima.repository.OrgaoGestorMeteorologiaRepository;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgaoGestorMeteorologiaService {

    private final OrgaoGestorMeteorologiaRepository orgaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DistritoMonitoramentoClimaticoRepository distritoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<OgmResponse> listarTodos() {
        return orgaoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OgmResponse buscarPorId(Long id) {
        return orgaoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("OGM nao encontrado com o ID informado"));
    }

    @Transactional
    public OgmResponse cadastrar(OgmRequest req) {
        String sigla = AdminValidation.textoObrigatorio(req.sigla(), "Sigla");
        String nome = AdminValidation.textoObrigatorio(req.nome(), "Nome");
        Long diretorId = AdminValidation.idObrigatorio(req.diretorGeralId(), "Diretor Geral");

        if (orgaoRepository.existsBySiglaIgnoreCase(sigla)) {
            throw new IllegalStateException("Sigla ja cadastrada para outro OGM");
        }
        if (orgaoRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Nome ja cadastrado para outro OGM");
        }

        Usuario diretor = usuarioRepository.findById(diretorId)
                .orElseThrow(() -> new EntityNotFoundException("Diretor geral nao encontrado"));
        AdminValidation.requireAtivo(diretor.getStatus(), "Diretor geral");

        if (orgaoRepository.existsByDiretorGeral_Id(diretor.getId())) {
            throw new IllegalStateException("Usuario ja e diretor geral de outro OGM");
        }

        var ogm = new OrgaoGestorMeteorologia();
        ogm.setSigla(sigla);
        ogm.setNome(nome);
        ogm.setEmailCentral(AdminValidation.email(req.emailCentral()));
        ogm.setTelefoneCentral(req.telefoneCentral() != null ? req.telefoneCentral().trim() : null);
        ogm.setDiretorGeral(diretor);
        ogm.setStatus(StatusEntidade.ATIVO);

        mapearSetores(req.setores(), ogm);

        ogm = orgaoRepository.save(ogm);
        auditoriaService.registrar("CADASTRAR", "OrgaoGestorMeteorologia", ogm.getId(), "sigla=" + ogm.getSigla());
        return toResponse(ogm);
    }

    @Transactional
    public OgmResponse atualizar(Long id, OgmRequest req) {
        var ogm = orgaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OGM nao encontrado com o ID informado"));

        String sigla = AdminValidation.textoObrigatorio(req.sigla(), "Sigla");
        String nome = AdminValidation.textoObrigatorio(req.nome(), "Nome");
        Long diretorId = AdminValidation.idObrigatorio(req.diretorGeralId(), "Diretor Geral");

        if (orgaoRepository.existsBySiglaIgnoreCaseAndIdNot(sigla, id)) {
            throw new IllegalStateException("Sigla ja cadastrada para outro OGM");
        }
        if (orgaoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new IllegalStateException("Nome ja cadastrado para outro OGM");
        }

        Usuario diretor = usuarioRepository.findById(diretorId)
                .orElseThrow(() -> new EntityNotFoundException("Diretor geral nao encontrado"));
        AdminValidation.requireAtivo(diretor.getStatus(), "Diretor geral");

        if (orgaoRepository.existsByDiretorGeral_IdAndIdNot(diretor.getId(), id)) {
            throw new IllegalStateException("Usuario ja e diretor geral de outro OGM");
        }

        ogm.setSigla(sigla);
        ogm.setNome(nome);
        ogm.setEmailCentral(AdminValidation.email(req.emailCentral()));
        ogm.setTelefoneCentral(req.telefoneCentral() != null ? req.telefoneCentral().trim() : null);
        ogm.setDiretorGeral(diretor);

        ogm.getSetores().clear();
        mapearSetores(req.setores(), ogm);

        ogm = orgaoRepository.save(ogm);
        auditoriaService.registrar("ATUALIZAR", "OrgaoGestorMeteorologia", ogm.getId(), "sigla=" + ogm.getSigla());
        return toResponse(ogm);
    }

    @Transactional
    public void inativar(Long id) {
        var ogm = orgaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OGM nao encontrado com o ID informado"));

        if (ogm.getStatus() == StatusEntidade.INATIVO) {
            throw new IllegalStateException("OGM ja esta inativo");
        }

        if (distritoRepository.existsByOrgaoGestor_IdAndStatus(id, StatusEntidade.ATIVO)) {
            throw new IllegalStateException("Nao e possivel inativar OGM com Distritos de Monitoramento Climatico ativos vinculados");
        }

        ogm.setStatus(StatusEntidade.INATIVO);
        orgaoRepository.save(ogm);
        auditoriaService.registrar("INATIVAR", "OrgaoGestorMeteorologia", ogm.getId(), "sigla=" + ogm.getSigla());
    }

    private void mapearSetores(List<SetorDTO> setoresDTO, OrgaoGestorMeteorologia ogm) {
        if (setoresDTO == null) return;
        for (SetorDTO dto : setoresDTO) {
            var setor = new SetorCoordenadoria();
            setor.setNome(AdminValidation.textoObrigatorio(dto.nome(), "Nome do setor"));
            setor.setNomeacaoResponsavel(AdminValidation.textoObrigatorio(dto.nomeacaoResponsavel(), "Responsavel pelo setor"));
            setor.setOrgaoGestor(ogm);
            ogm.getSetores().add(setor);
        }
    }

    private OgmResponse toResponse(OrgaoGestorMeteorologia ogm) {
        Long diretorId = ogm.getDiretorGeral() != null ? ogm.getDiretorGeral().getId() : null;
        String diretorNome = ogm.getDiretorGeral() != null ? ogm.getDiretorGeral().getNomeCompleto() : null;
        List<SetorDTO> setores = ogm.getSetores().stream()
                .map(s -> new SetorDTO(s.getId(), s.getNome(), s.getNomeacaoResponsavel()))
                .sorted(Comparator.comparing(SetorDTO::nome, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return new OgmResponse(
                ogm.getId(),
                ogm.getSigla(),
                ogm.getNome(),
                ogm.getEmailCentral(),
                ogm.getTelefoneCentral(),
                diretorId,
                diretorNome,
                setores,
                ogm.getDataCadastro(),
                ogm.getStatus()
        );
    }
}
