package com.unifil.cassino.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rodadas_crash")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RodadaCrash {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "multiplicador_crash", nullable = false, precision = 15, scale = 2)
    private BigDecimal multiplicadorCrash;

    @Column(name = "iniciada_em", nullable = false)
    private LocalDateTime iniciadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRodada status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aposta_id")
    private Aposta aposta;
}
