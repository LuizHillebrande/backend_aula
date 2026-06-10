package com.unifil.cassino;

import com.unifil.cassino.entity.Aposta;
import com.unifil.cassino.entity.Usuario;
import com.unifil.cassino.repository.ApostaRepository;
import com.unifil.cassino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ApostaRepository apostaRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .id(1L)
                .nome("Luiz Fernando")
                .cpf("123.456.789-00")
                .saldo(new BigDecimal("1000.00"))
                .build());

        apostaRepository.save(Aposta.builder()
                .usuario(usuario)
                .jogo("Crash")
                .valor(new BigDecimal("50.00"))
                .resultado("Vitória")
                .lucro(new BigDecimal("25.00"))
                .build());

        apostaRepository.save(Aposta.builder()
                .usuario(usuario)
                .jogo("Crash")
                .valor(new BigDecimal("20.00"))
                .resultado("Derrota")
                .lucro(new BigDecimal("-20.00"))
                .build());
    }
}
