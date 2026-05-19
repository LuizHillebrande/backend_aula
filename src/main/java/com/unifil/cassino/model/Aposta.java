package com.unifil.cassino.model;

public class Aposta {

    private Long id;
    private double valor;
    private String status;
    private String jogo;

    public Aposta() {
    }

    public Aposta(Long id, double valor, String status, String jogo) {
        this.id = id;
        this.valor = valor;
        this.status = status;
        this.jogo = jogo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJogo() {
        return jogo;
    }

    public void setJogo(String jogo) {
        this.jogo = jogo;
    }
}
