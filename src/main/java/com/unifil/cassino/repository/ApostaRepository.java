package com.unifil.cassino.repository;

import com.unifil.cassino.entity.Aposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApostaRepository extends JpaRepository<Aposta, Long> {

    List<Aposta> findByUsuarioIdOrderByIdDesc(Long usuarioId);
}
