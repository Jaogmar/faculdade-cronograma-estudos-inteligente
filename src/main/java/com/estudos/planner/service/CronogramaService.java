package com.estudos.planner.service;

import com.estudos.planner.model.MiniTema;
import com.estudos.planner.model.Objetivo;
import com.estudos.planner.model.Tarefa;
import com.estudos.planner.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CronogramaService {

    private final TarefaRepository tarefaRepository;

    public int calcularDiasUteis(LocalDate inicio, LocalDate fim, List<DayOfWeek> diasPermitidos) {
        int dias = 0;
        LocalDate atual = inicio;

        while (!atual.isAfter(fim)) {
            if (diasPermitidos.contains(atual.getDayOfWeek())) {
                dias++;
            }
            atual = atual.plusDays(1);
        }

        return dias;
    }

    public Map<String, Object> verificarViabilidade(Objetivo objetivo) {
        Map<String, Object> resultado = new HashMap<>();

        
        List<DayOfWeek> diasPermitidos = parsearDiasEstudo(objetivo.getDiasEstudo());

        
        int horasNecessarias = objetivo.getMiniTemas().stream()
                .filter(mt -> !mt.getRemovido())
                .mapToInt(MiniTema::getCargaHorariaEstimada)
                .sum();

        
        LocalDate hoje = LocalDate.now();
        int diasUteis = calcularDiasUteis(hoje, objetivo.getDataLimite(), diasPermitidos);

        
        int horasDisponiveis = diasUteis * objetivo.getHorasPorDia();

        boolean viavel = horasDisponiveis >= horasNecessarias;

        resultado.put("viavel", viavel);
        resultado.put("horasNecessarias", horasNecessarias);
        resultado.put("horasDisponiveis", horasDisponiveis);
        resultado.put("diasUteis", diasUteis);

        if (!viavel) {
            int horasFaltando = horasNecessarias - horasDisponiveis;
            resultado.put("horasFaltando", horasFaltando);

            
            List<MiniTema> temasOrdenados = objetivo.getMiniTemas().stream()
                    .filter(mt -> !mt.getRemovido())
                    .sorted(Comparator.comparingInt(MiniTema::getCargaHorariaEstimada).reversed())
                    .collect(Collectors.toList());

            List<Long> sugestaoRemocao = new ArrayList<>();
            int horasRemovidas = 0;

            for (MiniTema tema : temasOrdenados) {
                if (horasRemovidas >= horasFaltando) {
                    break;
                }
                sugestaoRemocao.add(tema.getId());
                horasRemovidas += tema.getCargaHorariaEstimada();
            }

            resultado.put("sugestaoRemocao", sugestaoRemocao);
        }

        return resultado;
    }

    @Transactional
    public void distribuirCargaHoraria(Objetivo objetivo) {
        log.info("Distribuindo carga horária para objetivo {}", objetivo.getId());

        
        List<Tarefa> tarefasExistentes = tarefaRepository.findByObjetivoOrderByDataAgendadaAsc(objetivo);
        tarefaRepository.deleteAll(tarefasExistentes);

        
        List<DayOfWeek> diasPermitidos = parsearDiasEstudo(objetivo.getDiasEstudo());

        
        List<MiniTema> miniTemasOrdenados = objetivo.getMiniTemas().stream()
                .filter(mt -> !mt.getRemovido())
                .sorted(Comparator.comparingInt(MiniTema::getCargaHorariaEstimada).reversed())
                .collect(Collectors.toList());

        
        List<LocalDate> datasDisponiveis = gerarDatasDisponiveis(
                LocalDate.now().plusDays(1),
                objetivo.getDataLimite(),
                diasPermitidos
        );

        if (datasDisponiveis.isEmpty()) {
            throw new RuntimeException("Nenhuma data disponível para agendar tarefas");
        }

        
        int indiceDia = 0;
        int horasUsadasNoDia = 0;

        for (MiniTema miniTema : miniTemasOrdenados) {
            int cargaTotal = miniTema.getCargaHorariaEstimada();

            
            List<SessaoEstudo> sessoes = dividirEmSessoes(miniTema, cargaTotal);

            for (SessaoEstudo sessao : sessoes) {
                
                if (horasUsadasNoDia + sessao.duracao > objetivo.getHorasPorDia()) {
                    
                    indiceDia++;
                    horasUsadasNoDia = 0;

                    if (indiceDia >= datasDisponiveis.size()) {
                        log.warn("Acabaram as datas disponíveis, ainda há {} sessões pendentes",
                                sessoes.size() - sessoes.indexOf(sessao));
                        break;
                    }
                }

                
                Tarefa tarefa = new Tarefa();
                tarefa.setObjetivo(objetivo);
                tarefa.setMiniTema(miniTema);
                tarefa.setDataAgendada(datasDisponiveis.get(indiceDia));
                tarefa.setDuracao(sessao.duracao * 60);
                tarefa.setTitulo(sessao.titulo);
                tarefa.setDescricao(sessao.descricao);
                tarefa.setConcluida(false);

                tarefaRepository.save(tarefa);

                horasUsadasNoDia += sessao.duracao;
            }
        }

        log.info("Distribuição concluída: {} tarefas criadas",
                tarefaRepository.findByObjetivoOrderByDataAgendadaAsc(objetivo).size());
    }

    private List<SessaoEstudo> dividirEmSessoes(MiniTema miniTema, int cargaTotal) {
        List<SessaoEstudo> sessoes = new ArrayList<>();

        
        int horasFundamentos = (int) Math.ceil(cargaTotal * 0.4);
        if (horasFundamentos > 0) {
            int numSessoesFund = (int) Math.ceil(horasFundamentos / 2.0);
            int horasPorSessao = (int) Math.ceil((double) horasFundamentos / numSessoesFund);

            for (int i = 0; i < numSessoesFund; i++) {
                sessoes.add(new SessaoEstudo(
                        miniTema.getNome() + " - Fundamentos (Parte " + (i + 1) + ")",
                        "Estudo dos conceitos fundamentais e introdução ao tema",
                        Math.min(horasPorSessao, 2)
                ));
            }
        }

        
        int horasAprofundamento = (int) Math.ceil(cargaTotal * 0.4);
        if (horasAprofundamento > 0) {
            int numSessoesAprof = (int) Math.ceil(horasAprofundamento / 2.0);
            int horasPorSessao = (int) Math.ceil((double) horasAprofundamento / numSessoesAprof);

            for (int i = 0; i < numSessoesAprof; i++) {
                sessoes.add(new SessaoEstudo(
                        miniTema.getNome() + " - Aprofundamento (Parte " + (i + 1) + ")",
                        "Estudo detalhado e prática do conteúdo",
                        Math.min(horasPorSessao, 2)
                ));
            }
        }

        
        int horasRevisao = cargaTotal - horasFundamentos - horasAprofundamento;
        if (horasRevisao > 0) {
            sessoes.add(new SessaoEstudo(
                    miniTema.getNome() + " - Revisão e Consolidação",
                    "Revisão geral e consolidação do aprendizado",
                    horasRevisao
            ));
        }

        return sessoes;
    }

    private List<LocalDate> gerarDatasDisponiveis(LocalDate inicio, LocalDate fim, List<DayOfWeek> diasPermitidos) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate atual = inicio;

        while (!atual.isAfter(fim)) {
            if (diasPermitidos.contains(atual.getDayOfWeek())) {
                datas.add(atual);
            }
            atual = atual.plusDays(1);
        }

        return datas;
    }

    private List<DayOfWeek> parsearDiasEstudo(String diasEstudo) {
        List<DayOfWeek> dias = new ArrayList<>();

        if (diasEstudo == null || diasEstudo.isEmpty()) {
            return dias;
        }

        String[] partes = diasEstudo.split(",");
        Map<String, DayOfWeek> mapa = Map.of(
                "SEG", DayOfWeek.MONDAY,
                "TER", DayOfWeek.TUESDAY,
                "QUA", DayOfWeek.WEDNESDAY,
                "QUI", DayOfWeek.THURSDAY,
                "SEX", DayOfWeek.FRIDAY,
                "SAB", DayOfWeek.SATURDAY,
                "DOM", DayOfWeek.SUNDAY
        );

        for (String parte : partes) {
            String dia = parte.trim().toUpperCase();
            if (mapa.containsKey(dia)) {
                dias.add(mapa.get(dia));
            }
        }

        return dias;
    }

    private static class SessaoEstudo {
        String titulo;
        String descricao;
        int duracao;

        public SessaoEstudo(String titulo, String descricao, int duracao) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.duracao = duracao;
        }
    }
}
