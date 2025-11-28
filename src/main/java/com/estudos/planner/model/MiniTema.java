package com.estudos.planner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mini_temas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiniTema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objetivo_id", nullable = false)
    @NotNull(message = "Objetivo é obrigatório")
    private Objetivo objetivo;

    @NotBlank(message = "Nome do mini-tema é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Positive(message = "Carga horária deve ser positiva")
    @Column(name = "carga_horaria_estimada", nullable = false)
    private Integer cargaHorariaEstimada;

    @Column(nullable = false)
    private Integer ordem = 0;

    @Column(name = "sugerido_por_ia", nullable = false)
    private Boolean sugeridoPorIA = false;

    @Column(nullable = false)
    private Boolean removido = false;

    @PrePersist
    protected void onCreate() {
        if (sugeridoPorIA == null) {
            sugeridoPorIA = false;
        }
        if (removido == null) {
            removido = false;
        }
        if (ordem == null) {
            ordem = 0;
        }
    }
}
