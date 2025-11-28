package com.estudos.planner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "objetivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Objetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "Usuário é obrigatório")
    private Usuario usuario;

    @NotBlank(message = "Tema principal é obrigatório")
    @Column(name = "tema_principal", nullable = false, length = 200)
    private String temaPrincipal;

    @NotNull(message = "Data limite é obrigatória")
    @Column(name = "data_limite", nullable = false)
    private LocalDate dataLimite;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObjetivoStatus status = ObjetivoStatus.RASCUNHO;

    @Positive(message = "Horas por dia deve ser positivo")
    @Column(name = "horas_por_dia")
    private Integer horasPorDia;

    @Column(name = "dias_estudo", length = 50)
    private String diasEstudo;

    @OneToMany(mappedBy = "objetivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MiniTema> miniTemas = new ArrayList<>();

    @OneToMany(mappedBy = "objetivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Tarefa> tarefas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = ObjetivoStatus.RASCUNHO;
        }
    }

    public void adicionarMiniTema(MiniTema miniTema) {
        miniTemas.add(miniTema);
        miniTema.setObjetivo(this);
    }

    public void removerMiniTema(MiniTema miniTema) {
        miniTemas.remove(miniTema);
        miniTema.setObjetivo(null);
    }

    public void adicionarTarefa(Tarefa tarefa) {
        tarefas.add(tarefa);
        tarefa.setObjetivo(this);
    }
}
