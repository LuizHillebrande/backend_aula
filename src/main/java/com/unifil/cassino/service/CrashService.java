package com.unifil.cassino.service;

import com.unifil.cassino.dto.request.CrashIniciarRequest;
import com.unifil.cassino.dto.request.CrashRetirarRequest;
import com.unifil.cassino.dto.response.ApostaResponse;
import com.unifil.cassino.dto.response.CrashEstadoResponse;
import com.unifil.cassino.dto.response.CrashIniciarResponse;
import com.unifil.cassino.dto.response.UsuarioResponse;
import com.unifil.cassino.entity.Aposta;
import com.unifil.cassino.entity.RodadaCrash;
import com.unifil.cassino.entity.StatusRodada;
import com.unifil.cassino.entity.Usuario;
import com.unifil.cassino.exception.RecursoNaoEncontradoException;
import com.unifil.cassino.exception.RegraNegocioException;
import com.unifil.cassino.repository.ApostaRepository;
import com.unifil.cassino.repository.RodadaCrashRepository;
import com.unifil.cassino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrashService {

    private static final String JOGO_CRASH = "Crash";
    private static final String VITORIA = "Vitória";
    private static final String DERROTA = "Derrota";
    private static final BigDecimal INCREMENTO_POR_SEGUNDO = new BigDecimal("0.50");

    private final UsuarioRepository usuarioRepository;
    private final ApostaRepository apostaRepository;
    private final RodadaCrashRepository rodadaCrashRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public CrashIniciarResponse iniciarRodada(Long usuarioId, CrashIniciarRequest request) {
        validarValorPositivo(request.getValor());

        Usuario usuario = usuarioService.buscarEntidade(usuarioId);
        validarSaldoSuficiente(usuario, request.getValor());

        usuario.setSaldo(usuario.getSaldo().subtract(request.getValor()));
        usuarioRepository.save(usuario);

        String rodadaId = UUID.randomUUID().toString();
        rodadaCrashRepository.save(RodadaCrash.builder()
                .id(rodadaId)
                .usuario(usuario)
                .valor(request.getValor())
                .multiplicadorCrash(gerarMultiplicadorCrash())
                .iniciadaEm(LocalDateTime.now())
                .status(StatusRodada.ATIVA)
                .build());

        return CrashIniciarResponse.builder()
                .rodadaId(rodadaId)
                .usuario(UsuarioResponse.from(usuario))
                .build();
    }

    @Transactional
    public CrashEstadoResponse obterEstado(Long usuarioId, String rodadaId) {
        RodadaCrash rodada = buscarRodadaDoUsuario(usuarioId, rodadaId);

        if (rodada.getStatus() == StatusRodada.ATIVA) {
            BigDecimal multiplicadorAtual = calcularMultiplicadorAtual(rodada);
            if (crashou(multiplicadorAtual, rodada.getMultiplicadorCrash())) {
                return finalizarDerrota(rodada);
            }
            return CrashEstadoResponse.builder()
                    .rodadaId(rodada.getId())
                    .status(StatusRodada.ATIVA)
                    .multiplicadorAtual(multiplicadorAtual)
                    .build();
        }

        return construirEstadoEncerrado(rodada);
    }

    @Transactional
    public CrashEstadoResponse retirar(Long usuarioId, CrashRetirarRequest request) {
        RodadaCrash rodada = buscarRodadaDoUsuario(usuarioId, request.getRodadaId());

        if (rodada.getStatus() != StatusRodada.ATIVA) {
            throw new RegraNegocioException("Rodada já encerrada");
        }

        BigDecimal multiplicadorAtual = calcularMultiplicadorAtual(rodada);
        if (crashou(multiplicadorAtual, rodada.getMultiplicadorCrash())) {
            return finalizarDerrota(rodada);
        }

        return finalizarVitoria(rodada, multiplicadorAtual);
    }

    BigDecimal gerarMultiplicadorCrash() {
        int random = (int) (Math.random() * 900) + 101;
        return BigDecimal.valueOf(random, 2);
    }

    BigDecimal calcularMultiplicadorAtual(RodadaCrash rodada) {
        return calcularMultiplicadorAtual(rodada.getIniciadaEm(), rodada.getMultiplicadorCrash());
    }

    BigDecimal calcularMultiplicadorAtual(LocalDateTime iniciadaEm, BigDecimal multiplicadorCrash) {
        long elapsedMs = Duration.between(iniciadaEm, LocalDateTime.now()).toMillis();
        BigDecimal elapsedSec = BigDecimal.valueOf(elapsedMs)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        BigDecimal atual = BigDecimal.ONE
                .add(INCREMENTO_POR_SEGUNDO.multiply(elapsedSec))
                .setScale(2, RoundingMode.HALF_UP);

        if (atual.compareTo(multiplicadorCrash) >= 0) {
            return multiplicadorCrash;
        }
        return atual;
    }

    boolean crashou(BigDecimal multiplicadorAtual, BigDecimal multiplicadorCrash) {
        return multiplicadorAtual.compareTo(multiplicadorCrash) >= 0;
    }

    ResultadoAposta calcularResultado(BigDecimal valor, BigDecimal multiplicadorRetirada, BigDecimal multiplicadorCrash) {
        if (multiplicadorRetirada.compareTo(multiplicadorCrash) >= 0) {
            return new ResultadoAposta(DERROTA, valor.negate(), false);
        }
        BigDecimal lucro = valor.multiply(multiplicadorRetirada.subtract(BigDecimal.ONE))
                .setScale(2, RoundingMode.HALF_UP);
        return new ResultadoAposta(VITORIA, lucro, true);
    }

    private CrashEstadoResponse finalizarVitoria(RodadaCrash rodada, BigDecimal multiplicadorRetirada) {
        ResultadoAposta resultado = calcularResultado(
                rodada.getValor(),
                multiplicadorRetirada,
                rodada.getMultiplicadorCrash()
        );

        Usuario usuario = rodada.getUsuario();
        usuario.setSaldo(usuario.getSaldo().add(rodada.getValor().add(resultado.lucro())));
        usuarioRepository.save(usuario);

        rodada.setStatus(StatusRodada.RETIRADA);
        rodadaCrashRepository.save(rodada);

        Aposta aposta = apostaRepository.save(Aposta.builder()
                .usuario(usuario)
                .jogo(JOGO_CRASH)
                .valor(rodada.getValor())
                .resultado(resultado.resultado())
                .lucro(resultado.lucro())
                .build());

        rodada.setAposta(aposta);
        rodadaCrashRepository.save(rodada);

        return CrashEstadoResponse.builder()
                .rodadaId(rodada.getId())
                .status(StatusRodada.RETIRADA)
                .multiplicadorAtual(multiplicadorRetirada)
                .multiplicadorCrash(rodada.getMultiplicadorCrash())
                .aposta(ApostaResponse.from(aposta))
                .usuario(UsuarioResponse.from(usuario))
                .build();
    }

    private CrashEstadoResponse finalizarDerrota(RodadaCrash rodada) {
        if (rodada.getStatus() != StatusRodada.ATIVA) {
            return construirEstadoEncerrado(rodada);
        }

        rodada.setStatus(StatusRodada.CRASHOU);
        rodadaCrashRepository.save(rodada);

        Aposta aposta = apostaRepository.save(Aposta.builder()
                .usuario(rodada.getUsuario())
                .jogo(JOGO_CRASH)
                .valor(rodada.getValor())
                .resultado(DERROTA)
                .lucro(rodada.getValor().negate())
                .build());

        rodada.setAposta(aposta);
        rodadaCrashRepository.save(rodada);

        return CrashEstadoResponse.builder()
                .rodadaId(rodada.getId())
                .status(StatusRodada.CRASHOU)
                .multiplicadorAtual(rodada.getMultiplicadorCrash())
                .multiplicadorCrash(rodada.getMultiplicadorCrash())
                .aposta(ApostaResponse.from(aposta))
                .usuario(UsuarioResponse.from(rodada.getUsuario()))
                .build();
    }

    private CrashEstadoResponse construirEstadoEncerrado(RodadaCrash rodada) {
        return CrashEstadoResponse.builder()
                .rodadaId(rodada.getId())
                .status(rodada.getStatus())
                .multiplicadorCrash(rodada.getMultiplicadorCrash())
                .multiplicadorAtual(rodada.getMultiplicadorCrash())
                .aposta(rodada.getAposta() != null ? ApostaResponse.from(rodada.getAposta()) : null)
                .usuario(UsuarioResponse.from(rodada.getUsuario()))
                .build();
    }

    private RodadaCrash buscarRodadaDoUsuario(Long usuarioId, String rodadaId) {
        RodadaCrash rodada = rodadaCrashRepository.findById(rodadaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rodada não encontrada"));

        if (!rodada.getUsuario().getId().equals(usuarioId)) {
            throw new RecursoNaoEncontradoException("Rodada não encontrada");
        }
        return rodada;
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Valor deve ser maior que zero");
        }
    }

    private void validarSaldoSuficiente(Usuario usuario, BigDecimal valor) {
        if (usuario.getSaldo().compareTo(valor) < 0) {
            throw new RegraNegocioException("Saldo insuficiente");
        }
    }

    record ResultadoAposta(String resultado, BigDecimal lucro, boolean vitoria) {
    }
}
