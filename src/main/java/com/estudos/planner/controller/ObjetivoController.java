package com.estudos.planner.controller;

import com.estudos.planner.dto.MiniTemaDTO;
import com.estudos.planner.model.MiniTema;
import com.estudos.planner.model.Objetivo;
import com.estudos.planner.model.Usuario;
import com.estudos.planner.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/objetivos")
@RequiredArgsConstructor
@Slf4j
public class ObjetivoController {

    private final UsuarioService usuarioService;
    private final ObjetivoService objetivoService;
    private final GeminiService geminiService;
    private final CronogramaService cronogramaService;

    // ========== ETAPA 1: Tema + Data Limite ==========

    @GetMapping("/novo/etapa1")
    public String etapa1Form() {
        return "objetivo/wizard-etapa1";
    }

    @PostMapping("/novo/etapa1")
    public String etapa1Submit(
            @RequestParam String temaPrincipal,
            @RequestParam LocalDate dataLimite,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Validações
            if (temaPrincipal == null || temaPrincipal.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("erro", "Tema principal é obrigatório");
                return "redirect:/objetivos/novo/etapa1";
            }

            if (dataLimite.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("erro", "Data limite deve ser futura");
                return "redirect:/objetivos/novo/etapa1";
            }

            // Buscar usuário
            String email = authentication.getName();
            Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Criar rascunho
            Objetivo objetivo = objetivoService.criarRascunho(usuario, temaPrincipal, dataLimite);

            log.info("Objetivo {} criado na etapa 1", objetivo.getId());

            return "redirect:/objetivos/novo/etapa2/" + objetivo.getId();

        } catch (Exception e) {
            log.error("Erro na etapa 1", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar objetivo: " + e.getMessage());
            return "redirect:/objetivos/novo/etapa1";
        }
    }

    // ========== ETAPA 2: Sugestões da IA ==========

    @GetMapping("/novo/etapa2/{id}")
    public String etapa2Form(@PathVariable Long id, Model model) {
        Objetivo objetivo = objetivoService.buscarPorId(id);
        model.addAttribute("objetivo", objetivo);
        return "objetivo/wizard-etapa2";
    }

    @PostMapping("/{id}/sugerir-temas")
    @ResponseBody
    public ResponseEntity<List<MiniTemaDTO>> sugerirTemas(@PathVariable Long id) {
        try {
            Objetivo objetivo = objetivoService.buscarPorId(id);
            List<MiniTemaDTO> sugestoes = geminiService.sugerirMiniTemas(objetivo.getTemaPrincipal());

            log.info("Retornando {} sugestões para objetivo {}", sugestoes.size(), id);

            return ResponseEntity.ok(sugestoes);
        } catch (Exception e) {
            log.error("Erro ao buscar sugestões", e);
            // Retornar lista vazia para permitir entrada manual
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/novo/etapa2/{id}")
    public String etapa2Submit(
            @PathVariable Long id,
            @RequestBody List<MiniTemaDTO> miniTemas,
            RedirectAttributes redirectAttributes) {

        try {
            // Filtrar apenas selecionados
            List<MiniTemaDTO> selecionados = miniTemas.stream()
                    .filter(MiniTemaDTO::getSelecionado)
                    .collect(Collectors.toList());

            if (selecionados.isEmpty()) {
                redirectAttributes.addFlashAttribute("erro", "Selecione pelo menos um mini-tema");
                return "redirect:/objetivos/novo/etapa2/" + id;
            }

            objetivoService.adicionarMiniTemas(id, selecionados);

            log.info("Mini-temas adicionados ao objetivo {}", id);

            return "redirect:/objetivos/novo/etapa3/" + id;

        } catch (Exception e) {
            log.error("Erro na etapa 2", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar mini-temas: " + e.getMessage());
            return "redirect:/objetivos/novo/etapa2/" + id;
        }
    }

    // ========== ETAPA 3: Carga Horária ==========

    @GetMapping("/novo/etapa3/{id}")
    public String etapa3Form(@PathVariable Long id, Model model) {
        Objetivo objetivo = objetivoService.buscarPorId(id);

        // Calcular total de horas
        int totalHoras = objetivoService.calcularHorasTotaisNecessarias(objetivo);

        model.addAttribute("objetivo", objetivo);
        model.addAttribute("miniTemas", objetivo.getMiniTemas());
        model.addAttribute("totalHoras", totalHoras);

        return "objetivo/wizard-etapa3";
    }

    @PostMapping("/novo/etapa3/{id}")
    public String etapa3Submit(
            @PathVariable Long id,
            @RequestParam List<Long> miniTemaIds,
            @RequestParam List<Integer> cargas,
            RedirectAttributes redirectAttributes) {

        try {
            objetivoService.atualizarCargasHorarias(id, miniTemaIds, cargas);

            log.info("Cargas horárias atualizadas para objetivo {}", id);

            return "redirect:/objetivos/novo/etapa4/" + id;

        } catch (Exception e) {
            log.error("Erro na etapa 3", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar cargas: " + e.getMessage());
            return "redirect:/objetivos/novo/etapa3/" + id;
        }
    }

    // ========== ETAPA 4: Rotina + Validação ==========

    @GetMapping("/novo/etapa4/{id}")
    public String etapa4Form(@PathVariable Long id, Model model) {
        Objetivo objetivo = objetivoService.buscarPorId(id);
        int totalHoras = objetivoService.calcularHorasTotaisNecessarias(objetivo);

        model.addAttribute("objetivo", objetivo);
        model.addAttribute("totalHoras", totalHoras);

        return "objetivo/wizard-etapa4";
    }

    @PostMapping("/{id}/validar-viabilidade")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarViabilidade(
            @PathVariable Long id,
            @RequestParam Integer horasPorDia,
            @RequestParam String diasEstudo) {

        try {
            // Atualizar rotina temporariamente para validação
            objetivoService.configurarRotina(id, horasPorDia, diasEstudo);

            Objetivo objetivo = objetivoService.buscarPorId(id);
            Map<String, Object> resultado = cronogramaService.verificarViabilidade(objetivo);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("Erro ao validar viabilidade", e);
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/novo/etapa4/{id}")
    public String etapa4Submit(
            @PathVariable Long id,
            @RequestParam Integer horasPorDia,
            @RequestParam String diasEstudo,
            RedirectAttributes redirectAttributes) {

        try {
            // Configurar rotina
            objetivoService.configurarRotina(id, horasPorDia, diasEstudo);

            // Validar viabilidade
            Objetivo objetivo = objetivoService.buscarPorId(id);
            Map<String, Object> viabilidade = cronogramaService.verificarViabilidade(objetivo);

            if (!(Boolean) viabilidade.get("viavel")) {
                redirectAttributes.addFlashAttribute("erro",
                        "Tempo insuficiente! Ajuste a rotina ou remova alguns temas.");
                return "redirect:/objetivos/novo/etapa4/" + id;
            }

            // Gerar cronograma
            cronogramaService.distribuirCargaHoraria(objetivo);

            // Finalizar objetivo
            objetivoService.finalizarObjetivo(id);

            log.info("Objetivo {} finalizado com sucesso", id);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Objetivo criado com sucesso! Seu cronograma está pronto.");

            return "redirect:/objetivos/" + id;

        } catch (Exception e) {
            log.error("Erro na etapa 4", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao finalizar objetivo: " + e.getMessage());
            return "redirect:/objetivos/novo/etapa4/" + id;
        }
    }

    // ========== Visualização de Objetivo ==========

    @GetMapping("/{id}")
    public String verDetalhes(@PathVariable Long id, Model model) {
        Objetivo objetivo = objetivoService.buscarPorId(id);
        double progresso = objetivoService.calcularProgresso(objetivo);

        model.addAttribute("objetivo", objetivo);
        model.addAttribute("progresso", progresso);

        return "objetivo/detalhes";
    }

    @PostMapping("/{id}/excluir")
    public String excluirObjetivo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Objetivo objetivo = objetivoService.buscarPorId(id);
            String temaPrincipal = objetivo.getTemaPrincipal();

            objetivoService.excluir(id);

            log.info("Objetivo excluído com sucesso: {}", temaPrincipal);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Objetivo '" + temaPrincipal + "' excluído com sucesso!");

            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Erro ao excluir objetivo {}", id, e);
            redirectAttributes.addFlashAttribute("mensagemErro",
                "Erro ao excluir objetivo. Tente novamente.");
            return "redirect:/objetivos/" + id;
        }
    }
}
