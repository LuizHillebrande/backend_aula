package com.unifil.cassino.dto.response;

import com.unifil.cassino.entity.StatusRodada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrashEstadoResponse {

    private String rodadaId;
    private StatusRodada status;
    private BigDecimal multiplicadorAtual;
    private BigDecimal multiplicadorCrash;
    private ApostaResponse aposta;
    private UsuarioResponse usuario;
}
