package com.estudos.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiniTemaDTO {
    private String nome;
    private String descricao;
    private Integer horasEstimadas;
    private Boolean selecionado = true;
    private Boolean sugeridoPorIA = false;
}
