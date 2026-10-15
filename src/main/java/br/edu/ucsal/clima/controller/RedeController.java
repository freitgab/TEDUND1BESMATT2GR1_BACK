package br.edu.ucsal.clima.controller;

import br.edu.ucsal.clima.dto.RedeRequest;
import br.edu.ucsal.clima.dto.RedeResponse;
import br.edu.ucsal.clima.service.RedeMonitoramentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rede")
@RequiredArgsConstructor
public class RedeController {

    private final RedeMonitoramentoService service;

    @GetMapping
    public ResponseEntity<List<RedeResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RedeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RedeResponse> cadastrar(@Valid @RequestBody RedeRequest request) {
        RedeResponse response = service.cadastrar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RedeResponse> atualizar(@PathVariable Long id, @Valid @RequestBody RedeRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping(value = {"/{id}/inativar", "/inativar/{id}"})
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
