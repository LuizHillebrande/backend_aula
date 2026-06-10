package com.unifil.cassino.service;

import com.unifil.cassino.entity.Usuario;
import com.unifil.cassino.exception.RecursoNaoEncontradoException;
import com.unifil.cassino.exception.RegraNegocioException;
import com.unifil.cassino.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void depositar_deveSomarAoSaldo() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Luiz Fernando")
                .cpf("123.456.789-00")
                .saldo(new BigDecimal("1000.00"))
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = usuarioService.depositar(1L, new BigDecimal("100.00"));

        assertThat(response.getSaldo()).isEqualByComparingTo("1100.00");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void depositar_valorInvalido_deveLancarExcecao() {
        assertThatThrownBy(() -> usuarioService.depositar(1L, BigDecimal.ZERO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Valor deve ser maior que zero");
    }

    @Test
    void buscarPorId_usuarioInexistente_deveLancarExcecao() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }
}
