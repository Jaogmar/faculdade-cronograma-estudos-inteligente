package com.estudos.planner.service;

import com.estudos.planner.model.Tarefa;
import com.estudos.planner.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public List<Tarefa> listarPorPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
        return tarefaRepository.findByUsuarioIdAndPeriodo(usuarioId, inicio, fim);
    }

    public List<Tarefa> listarAtrasadas(Long usuarioId) {
        return tarefaRepository.findTarefasAtrasadas(usuarioId, LocalDate.now());
    }

    @Transactional
    public void concluir(Long tarefaId) {
        log.info("Marcando tarefa {} como concluída", tarefaId);

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + tarefaId));

        tarefa.concluir();
        tarefaRepository.save(tarefa);
    }

    @Transactional
    public void desmarcarConclusao(Long tarefaId) {
        log.info("Desmarcando conclusão da tarefa {}", tarefaId);

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + tarefaId));

        tarefa.desmarcarConclusao();
        tarefaRepository.save(tarefa);
    }

    @Transactional
    public void reagendar(Long tarefaId, LocalDate novaData) {
        log.info("Reagendando tarefa {} para {}", tarefaId, novaData);

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + tarefaId));

        tarefa.setDataAgendada(novaData);
        tarefaRepository.save(tarefa);
    }

    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + id));
    }
}
