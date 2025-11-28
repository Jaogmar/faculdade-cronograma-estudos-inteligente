package com.estudos.planner.controller;

import com.estudos.planner.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastroForm() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Tentativa de cadastro para email: {}", email);

        try {
            // Validação de senha
            if (!senha.equals(confirmarSenha)) {
                model.addAttribute("erro", "As senhas não coincidem");
                return "cadastro";
            }

            // Validação de senha mínima
            if (senha.length() < 6) {
                model.addAttribute("erro", "A senha deve ter no mínimo 6 caracteres");
                return "cadastro";
            }

            // Validação de nome
            if (nome == null || nome.trim().length() < 3) {
                model.addAttribute("erro", "O nome deve ter no mínimo 3 caracteres");
                return "cadastro";
            }

            // Cadastrar usuário
            usuarioService.cadastrar(nome.trim(), email.trim(), senha);

            log.info("Usuário cadastrado com sucesso: {}", email);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Cadastro realizado com sucesso! Faça login para continuar.");

            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            log.warn("Erro ao cadastrar: {}", e.getMessage());
            model.addAttribute("erro", e.getMessage());
            return "cadastro";
        } catch (Exception e) {
            log.error("Erro inesperado ao cadastrar usuário", e);
            model.addAttribute("erro", "Erro ao realizar cadastro. Tente novamente.");
            return "cadastro";
        }
    }
}
