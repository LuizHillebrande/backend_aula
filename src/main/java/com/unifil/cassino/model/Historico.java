package com.unifil.cassino.model;

import java.time.LocalDateTime;

public class Historico {

    private Long id;
    private String descricao;
    private LocalDateTime data;
    private double resultado;

    public Historico() {
    }

    public Historico(Long id, String descricao, LocalDateTime data, double resultado) {
        this.id = id;
        this.descricao = descricao;
        this.data = data;
        this.resultado = resultado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public double getResultado() {
        return resultado;
    }

    public void setResultado(double resultado) {
        this.resultado = resultado;
    }
}