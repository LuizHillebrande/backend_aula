package com.unifil.cassino.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CrashServiceTest {

    @InjectMocks
    private CrashService crashService;

    @Test
    void calcularResultado_vitoria_quandoRetiradaAntesDoCrash() {
        var resultado = crashService.calcularResultado(
                new BigDecimal("100.00"),
                new BigDecimal("1.50"),
                new BigDecimal("1.80")
        );

        assertThat(resultado.resultado()).isEqualTo("Vitória");
        assertThat(resultado.lucro()).isEqualByComparingTo("50.00");
        assertThat(resultado.vitoria()).isTrue();
    }

    @Test
    void calcularResultado_derrota_quandoRetiradaNoOuAposCrash() {
        var resultado = crashService.calcularResultado(
                new BigDecimal("100.00"),
                new BigDecimal("1.80"),
                new BigDecimal("1.80")
        );

        assertThat(resultado.resultado()).isEqualTo("Derrota");
        assertThat(resultado.lucro()).isEqualByComparingTo("-100.00");
        assertThat(resultado.vitoria()).isFalse();
    }

    @Test
    void calcularMultiplicadorAtual_sobeComOTempo() {
        LocalDateTime inicio = LocalDateTime.now().minusSeconds(2);
        BigDecimal crash = new BigDecimal("10.00");

        BigDecimal atual = crashService.calcularMultiplicadorAtual(inicio, crash);

        assertThat(atual).isEqualByComparingTo("2.00");
    }

    @Test
    void calcularMultiplicadorAtual_paraNoPontoDeCrash() {
        LocalDateTime inicio = LocalDateTime.now().minusSeconds(20);
        BigDecimal crash = new BigDecimal("1.80");

        BigDecimal atual = crashService.calcularMultiplicadorAtual(inicio, crash);

        assertThat(atual).isEqualByComparingTo("1.80");
        assertThat(crashService.crashou(atual, crash)).isTrue();
    }

    @Test
    void gerarMultiplicadorCrash_deveEstarEntreLimites() {
        for (int i = 0; i < 50; i++) {
            BigDecimal multiplicador = crashService.gerarMultiplicadorCrash();
            assertThat(multiplicador).isGreaterThanOrEqualTo(new BigDecimal("1.01"));
            assertThat(multiplicador).isLessThanOrEqualTo(new BigDecimal("10.00"));
        }
    }
}
