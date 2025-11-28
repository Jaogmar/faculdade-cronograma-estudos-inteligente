package com.estudos.planner.controller;

import com.estudos.planner.model.Objetivo;
import com.estudos.planner.model.Tarefa;
import com.estudos.planner.model.Usuario;
import com.estudos.planner.repository.ObjetivoRepository;
import com.estudos.planner.repository.TarefaRepository;
import com.estudos.planner.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UsuarioService usuarioService;
    private final ObjetivoRepository objetivoRepository;
    private final TarefaRepository tarefaRepository;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        log.info("Acessando dashboard para usuário: {}", email);

        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Buscar objetivos do usuário
        List<Objetivo> objetivos = objetivoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario);

        // Buscar tarefas do dia
        LocalDate hoje = LocalDate.now();
        List<Tarefa> tarefasHoje = tarefaRepository.findByDataAgendadaAndObjetivo_UsuarioIdOrderByDataAgendadaAsc(
                hoje, usuario.getId());

        // Buscar tarefas atrasadas
        List<Tarefa> tarefasAtrasadas = tarefaRepository.findTarefasAtrasadas(usuario.getId(), hoje);

        model.addAttribute("usuario", usuario);
        model.addAttribute("objetivos", objetivos);
        model.addAttribute("tarefasHoje", tarefasHoje);
        model.addAttribute("tarefasAtrasadas", tarefasAtrasadas);
        model.addAttribute("totalObjetivos", objetivos.size());

        return "dashboard";
    }
}
