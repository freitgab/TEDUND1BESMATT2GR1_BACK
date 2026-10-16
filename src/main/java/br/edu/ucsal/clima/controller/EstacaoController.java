package br.edu.ucsal.clima.controller;

import br.edu.ucsal.clima.dto.EstacaoRequest;
import br.edu.ucsal.clima.dto.EstacaoResponse;
import br.edu.ucsal.clima.service.EstacaoMeteorologicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/estacoes")
@RequiredArgsConstructor
public class EstacaoController {

    private final EstacaoMeteorologicaService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('USUARIO', 'COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<EstacaoResponse>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USUARIO', 'COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EstacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EstacaoResponse> cadastrar(@Valid @RequestBody EstacaoRequest request) {
        EstacaoResponse response = service.cadastrar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EstacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody EstacaoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping(value = {"/{id}/inativar", "/inativar/{id}"})
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
