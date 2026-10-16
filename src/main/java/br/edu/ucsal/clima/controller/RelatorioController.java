package br.edu.ucsal.clima.controller;

import br.edu.ucsal.clima.dto.RelatorioColaboradorGeralDTO;
import br.edu.ucsal.clima.dto.RelatorioEstruturaOrganizacionalDTO;
import br.edu.ucsal.clima.dto.RelatorioTecnicosDTO;
import br.edu.ucsal.clima.dto.RelatorioUsuarioSolicitacoesDTO;
import br.edu.ucsal.clima.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/admin/estrutura-organizacional")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RelatorioEstruturaOrganizacionalDTO> relatorioEstruturaOrganizacional() {
        return ResponseEntity.ok(relatorioService.emitirRelatorioEstruturaOrganizacional());
    }

    @GetMapping("/admin/tecnicos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RelatorioTecnicosDTO> relatorioTecnicos() {
        return ResponseEntity.ok(relatorioService.emitirRelatorioTecnicos());
    }

    @GetMapping("/colaborador/geral")
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<RelatorioColaboradorGeralDTO> relatorioColaboradorGeral() {
        return ResponseEntity.ok(relatorioService.emitirRelatorioColaboradorGeral());
    }

    @GetMapping("/usuario/minhas-solicitacoes")
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR')")
    public ResponseEntity<RelatorioUsuarioSolicitacoesDTO> relatorioMinhasSolicitacoes() {
        return ResponseEntity.ok(relatorioService.emitirRelatorioMinhasSolicitacoes());
    }
}
