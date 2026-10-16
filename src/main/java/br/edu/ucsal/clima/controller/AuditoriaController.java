package br.edu.ucsal.clima.controller;

import br.edu.ucsal.clima.domain.entity.LogAuditoria;
import br.edu.ucsal.clima.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<LogAuditoria>> listar(
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidadeAfetada,
            @RequestParam(required = false) String usuarioEmail
    ) {
        return ResponseEntity.ok(auditoriaService.listarLogs(acao, entidadeAfetada, usuarioEmail));
    }
}
