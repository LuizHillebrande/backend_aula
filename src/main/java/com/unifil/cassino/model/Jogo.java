package com.unifil.cassino.model;

public class Jogo {

    private Long id;
    private String nome;
    private String tipo;
    private boolean ativo;

    public Jogo() {
    }

    public Jogo(Long id, String nome, String tipo, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}