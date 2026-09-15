package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "alimento_macros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlimentoMacro {

    @EmbeddedId
    private AlimentoMacroId id;

    @ManyToOne
    @MapsId("idAlimento")
    @JoinColumn(name = "id_alimento")
    private Alimento alimento;

    @ManyToOne
    @MapsId("idMacros")
    @JoinColumn(name = "id_macros")
    private MacroNutriente macroNutriente;

    @Column(name = "gramas_por_100g")
    private BigDecimal gramasPor100g;

}
