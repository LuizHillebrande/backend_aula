package com.unifil.cassino.controller;

import com.unifil.cassino.dto.request.CrashIniciarRequest;
import com.unifil.cassino.dto.request.CrashRetirarRequest;
import com.unifil.cassino.dto.request.DepositoRequest;
import com.unifil.cassino.dto.response.ApostaResponse;
import com.unifil.cassino.dto.response.CrashEstadoResponse;
import com.unifil.cassino.dto.response.CrashIniciarResponse;
import com.unifil.cassino.dto.response.UsuarioResponse;
import com.unifil.cassino.service.ApostaService;
import com.unifil.cassino.service.CrashService;
import com.unifil.cassino.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ApostaService apostaService;
    private final CrashService crashService;

    @GetMapping("/{id}")
    public UsuarioResponse buscarUsuario(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping("/{id}/deposito")
    public UsuarioResponse depositar(@PathVariable Long id, @Valid @RequestBody DepositoRequest request) {
        return usuarioService.depositar(id, request.getValor());
    }

    @GetMapping("/{id}/apostas")
    public List<ApostaResponse> listarApostas(@PathVariable Long id) {
        return apostaService.listarPorUsuario(id);
    }

    @PostMapping("/{id}/apostas/crash/iniciar")
    @ResponseStatus(HttpStatus.CREATED)
    public CrashIniciarResponse iniciarRodadaCrash(
            @PathVariable Long id,
            @Valid @RequestBody CrashIniciarRequest request) {
        return crashService.iniciarRodada(id, request);
    }

    @GetMapping("/{id}/apostas/crash/rodadas/{rodadaId}")
    public CrashEstadoResponse obterEstadoRodada(
            @PathVariable Long id,
            @PathVariable String rodadaId) {
        return crashService.obterEstado(id, rodadaId);
    }

    @PostMapping("/{id}/apostas/crash/retirar")
    public CrashEstadoResponse retirarRodadaCrash(
            @PathVariable Long id,
            @Valid @RequestBody CrashRetirarRequest request) {
        return crashService.retirar(id, request);
    }
}
