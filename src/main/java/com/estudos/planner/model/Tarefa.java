package com.estudos.planner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objetivo_id", nullable = false)
    @NotNull(message = "Objetivo é obrigatório")
    private Objetivo objetivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mini_tema_id", nullable = false)
    @NotNull(message = "Mini-tema é obrigatório")
    private MiniTema miniTema;

    @NotNull(message = "Data agendada é obrigatória")
    @Column(name = "data_agendada", nullable = false)
    private LocalDate dataAgendada;

    @Positive(message = "Duração deve ser positiva")
    @Column(nullable = false)
    private Integer duracao;

    @Column(nullable = false)
    private Boolean concluida = false;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(length = 100)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @PrePersist
    protected void onCreate() {
        if (concluida == null) {
            concluida = false;
        }
    }

    @Transient
    public boolean isEmAtraso() {
        if (concluida) {
            return false;
        }
        return dataAgendada.isBefore(LocalDate.now());
    }

    public void concluir() {
        this.concluida = true;
        this.dataConclusao = LocalDateTime.now();
    }

    public void desmarcarConclusao() {
        this.concluida = false;
        this.dataConclusao = null;
    }
}
