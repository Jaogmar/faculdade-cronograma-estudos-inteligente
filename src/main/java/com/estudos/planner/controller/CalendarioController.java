package com.estudos.planner.controller;

import com.estudos.planner.model.Objetivo;
import com.estudos.planner.model.Tarefa;
import com.estudos.planner.model.Usuario;
import com.estudos.planner.repository.ObjetivoRepository;
import com.estudos.planner.repository.TarefaRepository;
import com.estudos.planner.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calendario")
@RequiredArgsConstructor
@Slf4j
public class CalendarioController {

    private final UsuarioService usuarioService;
    private final ObjetivoRepository objetivoRepository;
    private final TarefaRepository tarefaRepository;

    @GetMapping
    public String calendario(Authentication authentication, Model model) {
        String email = authentication.getName();
        log.info("Acessando calendário para usuário: {}", email);

        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        
        List<Objetivo> objetivos = objetivoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("objetivos", objetivos);

        return "calendario";
    }

    @GetMapping("/tarefas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listarTarefas(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        
        LocalDate inicio = LocalDate.now().minusMonths(6);
        LocalDate fim = LocalDate.now().plusMonths(6);

        List<Tarefa> tarefas = tarefaRepository.findByUsuarioIdAndPeriodo(usuario.getId(), inicio, fim);

        
        List<Map<String, Object>> eventos = tarefas.stream()
                .map(this::tarefaParaEvento)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventos);
    }

    private Map<String, Object> tarefaParaEvento(Tarefa tarefa) {
        Map<String, Object> evento = new HashMap<>();

        evento.put("id", tarefa.getId());
        evento.put("title", tarefa.getTitulo());
        evento.put("start", tarefa.getDataAgendada().toString());
        evento.put("allDay", true);

        
        evento.put("extendedProps", Map.of(
                "tarefaId", tarefa.getId(),
                "objetivoId", tarefa.getObjetivo().getId(),
                "objetivoNome", tarefa.getObjetivo().getTemaPrincipal(),
                "miniTemaNome", tarefa.getMiniTema().getNome(),
                "descricao", tarefa.getDescricao() != null ? tarefa.getDescricao() : "",
                "duracao", tarefa.getDuracao(),
                "concluida", tarefa.getConcluida(),
                "emAtraso", tarefa.isEmAtraso()
        ));

        
        if (tarefa.getConcluida()) {
            evento.put("backgroundColor", "#10B981");
            evento.put("borderColor", "#059669");
            evento.put("className", "evento-concluido");
        } else if (tarefa.isEmAtraso()) {
            evento.put("backgroundColor", "#F59E0B");
            evento.put("borderColor", "#D97706");
            evento.put("className", "evento-atrasado");
        } else {
            evento.put("backgroundColor", "#3B82F6");
            evento.put("borderColor", "#2563EB");
            evento.put("className", "evento-pendente");
        }

        return evento;
    }
}
