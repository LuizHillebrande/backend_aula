package com.unifil.cassino.model;

public class Usuario {

    // ATRIBUTOS PRIVADOS
    private Long id;
    private String nome;
    private String email;
    private double saldo;

    // CONSTRUTOR VAZIO
    public Usuario() {
    }

    // CONSTRUTOR COMPLETO
    public Usuario(Long id, String nome, String email, double saldo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.saldo = saldo;
    }

    // GETTERS E SETTERS

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}
