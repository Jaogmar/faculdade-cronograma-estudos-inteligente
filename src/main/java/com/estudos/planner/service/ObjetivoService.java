package com.estudos.planner.service;

import com.estudos.planner.dto.MiniTemaDTO;
import com.estudos.planner.model.*;
import com.estudos.planner.repository.MiniTemaRepository;
import com.estudos.planner.repository.ObjetivoRepository;
import com.estudos.planner.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjetivoService {

    private final ObjetivoRepository objetivoRepository;
    private final MiniTemaRepository miniTemaRepository;
    private final TarefaRepository tarefaRepository;

    @Transactional
    public Objetivo criarRascunho(Usuario usuario, String temaPrincipal, LocalDate dataLimite) {
        log.info("Criando rascunho de objetivo para usuário {}: {}", usuario.getId(), temaPrincipal);

        Objetivo objetivo = new Objetivo();
        objetivo.setUsuario(usuario);
        objetivo.setTemaPrincipal(temaPrincipal);
        objetivo.setDataLimite(dataLimite);
        objetivo.setStatus(ObjetivoStatus.RASCUNHO);

        return objetivoRepository.save(objetivo);
    }

    @Transactional
    public void adicionarMiniTemas(Long objetivoId, List<MiniTemaDTO> miniTemasDTO) {
        log.info("Adicionando {} mini-temas ao objetivo {}", miniTemasDTO.size(), objetivoId);

        Objetivo objetivo = objetivoRepository.findById(objetivoId)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        // Remover mini-temas existentes
        objetivo.getMiniTemas().clear();

        // Adicionar novos mini-temas
        int ordem = 1;
        for (MiniTemaDTO dto : miniTemasDTO) {
            if (dto.getSelecionado()) {
                MiniTema miniTema = new MiniTema();
                miniTema.setObjetivo(objetivo);
                miniTema.setNome(dto.getNome());
                miniTema.setDescricao(dto.getDescricao());
                miniTema.setCargaHorariaEstimada(dto.getHorasEstimadas());
                miniTema.setOrdem(ordem++);
                miniTema.setSugeridoPorIA(dto.getSugeridoPorIA());
                miniTema.setRemovido(false);

                objetivo.getMiniTemas().add(miniTema);
            }
        }

        objetivoRepository.save(objetivo);
    }

    @Transactional
    public void atualizarCargasHorarias(Long objetivoId, List<Long> miniTemaIds, List<Integer> cargas) {
        log.info("Atualizando cargas horárias do objetivo {}", objetivoId);

        Objetivo objetivo = objetivoRepository.findById(objetivoId)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        for (int i = 0; i < miniTemaIds.size(); i++) {
            Long miniTemaId = miniTemaIds.get(i);
            Integer novaCarga = cargas.get(i);

            MiniTema miniTema = objetivo.getMiniTemas().stream()
                    .filter(mt -> mt.getId().equals(miniTemaId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Mini-tema não encontrado: " + miniTemaId));

            miniTema.setCargaHorariaEstimada(novaCarga);
        }

        objetivoRepository.save(objetivo);
    }

    @Transactional
    public void configurarRotina(Long objetivoId, Integer horasPorDia, String diasEstudo) {
        log.info("Configurando rotina do objetivo {}: {}h/dia, dias: {}", objetivoId, horasPorDia, diasEstudo);

        Objetivo objetivo = objetivoRepository.findById(objetivoId)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        objetivo.setHorasPorDia(horasPorDia);
        objetivo.setDiasEstudo(diasEstudo);

        objetivoRepository.save(objetivo);
    }

    @Transactional
    public void finalizarObjetivo(Long objetivoId) {
        log.info("Finalizando objetivo {}", objetivoId);

        Objetivo objetivo = objetivoRepository.findById(objetivoId)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado"));

        objetivo.setStatus(ObjetivoStatus.EM_ANDAMENTO);
        objetivoRepository.save(objetivo);
    }

    public Objetivo buscarPorId(Long id) {
        return objetivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado: " + id));
    }

    public List<Objetivo> listarPorUsuario(Usuario usuario) {
        return objetivoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario);
    }

    public int calcularHorasTotaisNecessarias(Objetivo objetivo) {
        return objetivo.getMiniTemas().stream()
                .filter(mt -> !mt.getRemovido())
                .mapToInt(MiniTema::getCargaHorariaEstimada)
                .sum();
    }

    public double calcularProgresso(Objetivo objetivo) {
        List<Tarefa> tarefas = tarefaRepository.findByObjetivoOrderByDataAgendadaAsc(objetivo);

        if (tarefas.isEmpty()) {
            return 0.0;
        }

        long tarefasConcluidas = tarefas.stream().filter(Tarefa::getConcluida).count();
        return (tarefasConcluidas * 100.0) / tarefas.size();
    }

    @Transactional
    public void excluir(Long objetivoId) {
        log.info("Excluindo objetivo {}", objetivoId);

        Objetivo objetivo = objetivoRepository.findById(objetivoId)
                .orElseThrow(() -> new RuntimeException("Objetivo não encontrado: " + objetivoId));

        // Excluir todas as tarefas associadas
        tarefaRepository.deleteAll(objetivo.getTarefas());

        // Excluir todos os mini-temas associados
        miniTemaRepository.deleteAll(objetivo.getMiniTemas());

        // Excluir o objetivo
        objetivoRepository.delete(objetivo);

        log.info("Objetivo {} excluído com sucesso", objetivoId);
    }
}
