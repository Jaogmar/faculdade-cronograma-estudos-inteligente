package com.estudos.planner.repository;

import com.estudos.planner.model.MiniTema;
import com.estudos.planner.model.Objetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiniTemaRepository extends JpaRepository<MiniTema, Long> {

    List<MiniTema> findByObjetivoAndRemovidoFalseOrderByOrdem(Objetivo objetivo);

    List<MiniTema> findByObjetivoIdAndRemovidoFalseOrderByOrdem(Long objetivoId);

    long countByObjetivoAndRemovidoFalse(Objetivo objetivo);
}
