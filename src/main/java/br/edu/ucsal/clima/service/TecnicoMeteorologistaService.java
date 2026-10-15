package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.DistritoMonitoramentoClimatico;
import br.edu.ucsal.clima.domain.entity.Perfil;
import br.edu.ucsal.clima.domain.entity.TecnicoMeteorologista;
import br.edu.ucsal.clima.domain.entity.Usuario;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.dto.JustificativaInativacaoDTO;
import br.edu.ucsal.clima.dto.TecnicoAtualizacaoRequest;
import br.edu.ucsal.clima.dto.TecnicoCadastroRequest;
import br.edu.ucsal.clima.dto.TecnicoEdicaoRequest;
import br.edu.ucsal.clima.dto.TecnicoResponse;
import br.edu.ucsal.clima.repository.DistritoMonitoramentoClimaticoRepository;
import br.edu.ucsal.clima.repository.PerfilRepository;
import br.edu.ucsal.clima.repository.TecnicoMeteorologistaRepository;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TecnicoMeteorologistaService {

    private final TecnicoMeteorologistaRepository tecnicoRepository;
    private final DistritoMonitoramentoClimaticoRepository dmcRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listarTodos() {
        return tecnicoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscarPorId(Long id) {
        return tecnicoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Tecnico nao encontrado com o ID informado"));
    }

    @Transactional
    public TecnicoResponse cadastrar(TecnicoCadastroRequest req) {
        String matricula = AdminValidation.textoObrigatorio(req.matricula(), "Matricula");
        String nomeCompleto = AdminValidation.textoObrigatorio(req.nomeCompleto(), "Nome completo");
        String email = AdminValidation.textoObrigatorio(req.email(), "Email");
        email = AdminValidation.email(email);
        Long distritoId = AdminValidation.idObrigatorio(req.distritoId(), "Distrito");

        if (tecnicoRepository.existsByMatriculaIgnoreCase(matricula)) {
            throw new IllegalStateException("Matricula ja cadastrada para outro tecnico");
        }
        if (tecnicoRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Email ja cadastrado para outro tecnico");
        }

        DistritoMonitoramentoClimatico dmc = dmcRepository.findById(distritoId)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado"));
        AdminValidation.requireAtivo(dmc.getStatus(), "DMC");

        Usuario usuario;
        if (req.usuarioId() != null) {
            usuario = usuarioRepository.findById(req.usuarioId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));
            AdminValidation.requireAtivo(usuario.getStatus(), "Usuario");
            if (tecnicoRepository.existsByUsuario_Id(usuario.getId())) {
                throw new IllegalStateException("Usuario ja vinculado a outro tecnico");
            }
            garantirPerfilColaborador(usuario);
        } else {
            if (usuarioRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalStateException("Email ja cadastrado para um usuario existente");
            }
            usuario = new Usuario();
            usuario.setNomeCompleto(nomeCompleto);
            usuario.setEmail(email);
            String senha = (req.senhaInicial() != null && !req.senhaInicial().isBlank())
                    ? req.senhaInicial()
                    : "Mudar@123";
            usuario.setSenhaHash(passwordEncoder.encode(senha));
            usuario.setTelefone(req.telefone() != null ? req.telefone().trim() : null);
            usuario.setStatus(StatusEntidade.ATIVO);
            garantirPerfilColaborador(usuario);
            usuario = usuarioRepository.save(usuario);
        }

        var tecnico = new TecnicoMeteorologista();
        tecnico.setMatricula(matricula);
        tecnico.setNomeCompleto(nomeCompleto);
        tecnico.setEmail(email);
        tecnico.setTelefone(req.telefone() != null ? req.telefone().trim() : null);
        tecnico.setDistrito(dmc);
        tecnico.setUsuario(usuario);
        tecnico.setStatus(StatusEntidade.ATIVO);

        tecnico = tecnicoRepository.save(tecnico);
        auditoriaService.registrar("CADASTRAR", "TecnicoMeteorologista", tecnico.getId(), "matricula=" + tecnico.getMatricula());
        return toResponse(tecnico);
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoEdicaoRequest req) {
        var tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tecnico nao encontrado com o ID informado"));

        String matricula = AdminValidation.textoObrigatorio(req.matricula(), "Matricula");
        String nomeCompleto = AdminValidation.textoObrigatorio(req.nomeCompleto(), "Nome completo");
        String email = AdminValidation.textoObrigatorio(req.email(), "Email");
        email = AdminValidation.email(email);
        Long distritoId = AdminValidation.idObrigatorio(req.distritoId(), "Distrito");

        if (tecnicoRepository.existsByMatriculaIgnoreCaseAndIdNot(matricula, id)) {
            throw new IllegalStateException("Matricula ja cadastrada para outro tecnico");
        }
        if (tecnicoRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new IllegalStateException("Email ja cadastrado para outro tecnico");
        }

        DistritoMonitoramentoClimatico dmc = dmcRepository.findById(distritoId)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado"));
        AdminValidation.requireAtivo(dmc.getStatus(), "DMC");

        if (tecnico.getUsuario() != null) {
            Usuario usuario = tecnico.getUsuario();
            if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, usuario.getId())) {
                throw new IllegalStateException("Email ja cadastrado para outro usuario");
            }
            usuario.setNomeCompleto(nomeCompleto);
            usuario.setEmail(email);
            usuario.setTelefone(req.telefone() != null ? req.telefone().trim() : null);
            usuarioRepository.save(usuario);
        }

        tecnico.setMatricula(matricula);
        tecnico.setNomeCompleto(nomeCompleto);
        tecnico.setEmail(email);
        tecnico.setTelefone(req.telefone() != null ? req.telefone().trim() : null);
        tecnico.setDistrito(dmc);

        tecnico = tecnicoRepository.save(tecnico);
        auditoriaService.registrar("ATUALIZAR", "TecnicoMeteorologista", tecnico.getId(), "matricula=" + tecnico.getMatricula());
        return toResponse(tecnico);
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoAtualizacaoRequest req) {
        return atualizar(id, new TecnicoEdicaoRequest(req.matricula(), req.nomeCompleto(), req.email(), req.telefone(), req.distritoId()));
    }

    @Transactional
    public void inativar(Long id, JustificativaInativacaoDTO dto) {
        var tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tecnico nao encontrado com o ID informado"));

        if (tecnico.getStatus() == StatusEntidade.INATIVO) {
            throw new IllegalStateException("Tecnico ja esta inativo");
        }

        if (dto == null || dto.justificativa() == null || dto.justificativa().isBlank()) {
            throw new IllegalArgumentException("Justificativa de inativacao e obrigatoria");
        }

        tecnico.inativarComJustificativa(dto.justificativa());

        if (tecnico.getUsuario() != null) {
            Usuario usuario = tecnico.getUsuario();
            usuario.setStatus(StatusEntidade.INATIVO);
            usuarioRepository.save(usuario);
        }

        tecnicoRepository.save(tecnico);
        auditoriaService.registrar("INATIVAR", "TecnicoMeteorologista", tecnico.getId(), "justificativa=" + dto.justificativa().trim());
    }

    @Transactional
    public void inativarComHistorico(Long id, JustificativaInativacaoDTO dto) {
        inativar(id, dto);
    }

    @Transactional
    public void inativarComHistorico(Long id, String justificativa) {
        inativar(id, new JustificativaInativacaoDTO(justificativa));
    }

    private void garantirPerfilColaborador(Usuario usuario) {
        boolean temColaborador = usuario.getPerfis().stream()
                .anyMatch(p -> p.getNome() == NomePerfil.COLABORADOR);
        if (!temColaborador) {
            Perfil perfilColaborador = perfilRepository.findByNome(NomePerfil.COLABORADOR)
                    .orElseThrow(() -> new IllegalStateException("Perfil COLABORADOR nao encontrado no sistema"));
            usuario.getPerfis().add(perfilColaborador);
        }
    }

    private TecnicoResponse toResponse(TecnicoMeteorologista tecnico) {
        Long usuarioId = tecnico.getUsuario() != null ? tecnico.getUsuario().getId() : null;
        Long distritoId = tecnico.getDistrito() != null ? tecnico.getDistrito().getId() : null;
        String distritoNome = tecnico.getDistrito() != null ? tecnico.getDistrito().getNome() : null;

        return new TecnicoResponse(
                tecnico.getId(),
                tecnico.getMatricula(),
                tecnico.getNomeCompleto(),
                tecnico.getEmail(),
                tecnico.getTelefone(),
                usuarioId,
                distritoId,
                distritoNome,
                tecnico.getDataCadastro(),
                tecnico.getStatus(),
                tecnico.getMotivoInativacao()
        );
    }
}
