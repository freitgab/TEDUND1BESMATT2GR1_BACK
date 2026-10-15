package br.edu.ucsal.clima.controller;

import br.edu.ucsal.clima.dto.JustificativaInativacaoDTO;
import br.edu.ucsal.clima.dto.TecnicoCadastroRequest;
import br.edu.ucsal.clima.dto.TecnicoEdicaoRequest;
import br.edu.ucsal.clima.dto.TecnicoResponse;
import br.edu.ucsal.clima.service.TecnicoMeteorologistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tecnico")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoMeteorologistaService service;

    @GetMapping
    public ResponseEntity<List<TecnicoResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TecnicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TecnicoResponse> cadastrar(@Valid @RequestBody TecnicoCadastroRequest request) {
        TecnicoResponse response = service.cadastrar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TecnicoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody TecnicoEdicaoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping(value = {"/{id}/inativar", "/inativar/{id}"})
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id, @Valid @RequestBody JustificativaInativacaoDTO dto) {
        service.inativar(id, dto);
        return ResponseEntity.noContent().build();
    }
}
