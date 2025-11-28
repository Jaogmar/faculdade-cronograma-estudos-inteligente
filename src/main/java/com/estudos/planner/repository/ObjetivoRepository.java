package com.estudos.planner.repository;

import com.estudos.planner.model.Objetivo;
import com.estudos.planner.model.ObjetivoStatus;
import com.estudos.planner.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjetivoRepository extends JpaRepository<Objetivo, Long> {

    List<Objetivo> findByUsuarioOrderByDataCriacaoDesc(Usuario usuario);

    List<Objetivo> findByUsuarioAndStatusOrderByDataLimiteAsc(Usuario usuario, ObjetivoStatus status);

    List<Objetivo> findByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);

    long countByUsuarioAndStatus(Usuario usuario, ObjetivoStatus status);
}
