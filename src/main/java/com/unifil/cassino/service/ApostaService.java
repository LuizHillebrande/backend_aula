package com.unifil.cassino.service;

import com.unifil.cassino.dto.response.ApostaResponse;
import com.unifil.cassino.repository.ApostaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApostaService {

    private final ApostaRepository apostaRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public List<ApostaResponse> listarPorUsuario(Long usuarioId) {
        usuarioService.buscarEntidade(usuarioId);
        return apostaRepository.findByUsuarioIdOrderByIdDesc(usuarioId).stream()
                .map(ApostaResponse::from)
                .toList();
    }
}
