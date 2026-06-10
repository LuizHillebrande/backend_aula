package com.unifil.cassino.dto.response;

import com.unifil.cassino.entity.Aposta;
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
public class ApostaResponse {

    private Long id;
    private String jogo;
    private BigDecimal valor;
    private String resultado;
    private BigDecimal lucro;

    public static ApostaResponse from(Aposta aposta) {
        return ApostaResponse.builder()
                .id(aposta.getId())
                .jogo(aposta.getJogo())
                .valor(aposta.getValor())
                .resultado(aposta.getResultado())
                .lucro(aposta.getLucro())
                .build();
    }
}
