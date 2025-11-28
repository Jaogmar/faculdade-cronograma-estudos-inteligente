package com.estudos.planner.controller;

import com.estudos.planner.service.TarefaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/tarefas")
@RequiredArgsConstructor
@Slf4j
public class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping("/{id}/concluir")
    @ResponseBody
    public ResponseEntity<Void> concluirTarefa(@PathVariable Long id) {
        try {
            tarefaService.concluir(id);
            log.info("Tarefa {} marcada como concluída", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao concluir tarefa {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/desmarcar")
    @ResponseBody
    public ResponseEntity<Void> desmarcarTarefa(@PathVariable Long id) {
        try {
            tarefaService.desmarcarConclusao(id);
            log.info("Tarefa {} desmarcada", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao desmarcar tarefa {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reagendar")
    @ResponseBody
    public ResponseEntity<Void> reagendarTarefa(
            @PathVariable Long id,
            @RequestParam String novaData) {
        try {
            LocalDate data = LocalDate.parse(novaData);
            tarefaService.reagendar(id, data);
            log.info("Tarefa {} reagendada para {}", id, data);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao reagendar tarefa {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
