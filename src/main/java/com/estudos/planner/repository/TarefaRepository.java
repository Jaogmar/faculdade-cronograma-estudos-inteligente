package com.estudos.planner.repository;

import com.estudos.planner.model.Objetivo;
import com.estudos.planner.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByObjetivoOrderByDataAgendadaAsc(Objetivo objetivo);

    List<Tarefa> findByObjetivoIdOrderByDataAgendadaAsc(Long objetivoId);

    List<Tarefa> findByDataAgendadaAndObjetivo_UsuarioIdOrderByDataAgendadaAsc(
            LocalDate dataAgendada, Long usuarioId);

    @Query("SELECT t FROM Tarefa t WHERE t.objetivo.usuario.id = :usuarioId " +
           "AND t.dataAgendada BETWEEN :inicio AND :fim " +
           "ORDER BY t.dataAgendada ASC")
    List<Tarefa> findByUsuarioIdAndPeriodo(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @Query("SELECT t FROM Tarefa t WHERE t.objetivo.usuario.id = :usuarioId " +
           "AND t.concluida = false AND t.dataAgendada < :hoje " +
           "ORDER BY t.dataAgendada ASC")
    List<Tarefa> findTarefasAtrasadas(
            @Param("usuarioId") Long usuarioId,
            @Param("hoje") LocalDate hoje);

    List<Tarefa> findByObjetivoAndConcluidaFalseOrderByDataAgendadaAsc(Objetivo objetivo);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.objetivo = :objetivo AND t.concluida = true")
    long countConcluidasByObjetivo(@Param("objetivo") Objetivo objetivo);

    @Query("SELECT SUM(t.duracao) FROM Tarefa t WHERE t.objetivo = :objetivo AND t.concluida = true")
    Long sumDuracaoConcluidasByObjetivo(@Param("objetivo") Objetivo objetivo);
}
