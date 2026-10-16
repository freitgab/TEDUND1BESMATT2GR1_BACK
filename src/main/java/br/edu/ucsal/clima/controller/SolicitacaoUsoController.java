package br.edu.ucsal.clima.controller;

import br.edu.ucsal.clima.dto.ReprovacaoRequest;
import br.edu.ucsal.clima.dto.SolicitacaoUsoRequest;
import br.edu.ucsal.clima.dto.SolicitacaoUsoResponse;
import br.edu.ucsal.clima.service.SolicitacaoUsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes-uso")
@RequiredArgsConstructor
public class SolicitacaoUsoController {

    private final SolicitacaoUsoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('USUARIO', 'COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<SolicitacaoUsoResponse>> listar(
            @RequestParam(name = "me", defaultValue = "false") boolean me
    ) {
        return ResponseEntity.ok(service.listar(me));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USUARIO', 'COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<SolicitacaoUsoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR')")
    public ResponseEntity<SolicitacaoUsoResponse> criar(@Valid @RequestBody SolicitacaoUsoRequest request) {
        SolicitacaoUsoResponse response = service.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR')")
    public ResponseEntity<SolicitacaoUsoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitacaoUsoRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping(value = {"/{id}/inativar", "/inativar/{id}"})
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = {"/{id}/liberar", "/liberar/{id}"})
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR')")
    public ResponseEntity<SolicitacaoUsoResponse> liberar(@PathVariable Long id) {
        return ResponseEntity.ok(service.liberarEstacao(id));
    }

    @PatchMapping(value = {"/{id}/aprovar", "/aprovar/{id}"})
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<SolicitacaoUsoResponse> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprovar(id));
    }

    @PatchMapping(value = {"/{id}/reprovar", "/reprovar/{id}"})
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<SolicitacaoUsoResponse> reprovar(
            @PathVariable Long id,
            @Valid @RequestBody ReprovacaoRequest request
    ) {
        return ResponseEntity.ok(service.reprovar(id, request));
    }
}
