package com.unifil.cassino.service;

import com.unifil.cassino.dto.response.UsuarioResponse;
import com.unifil.cassino.entity.Usuario;
import com.unifil.cassino.exception.RecursoNaoEncontradoException;
import com.unifil.cassino.exception.RegraNegocioException;
import com.unifil.cassino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.from(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse depositar(Long id, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Valor deve ser maior que zero");
        }

        Usuario usuario = buscarEntidade(id);
        usuario.setSaldo(usuario.getSaldo().add(valor));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
