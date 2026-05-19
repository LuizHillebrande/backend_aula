package com.unifil.cassino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.unifil.cassino.model.Usuario;

@SpringBootApplication
public class CassinoApplication {

	public static void main(String[] args) {

		SpringApplication.run(CassinoApplication.class, args);

		Usuario usuario = new Usuario();

		usuario.setId(1L);
		usuario.setNome("Luiz");
		usuario.setSaldo(500.0);

		System.out.println("ID: " + usuario.getId());
		System.out.println("Nome: " + usuario.getNome());
		System.out.println("Saldo: " + usuario.getSaldo());
	}
}

