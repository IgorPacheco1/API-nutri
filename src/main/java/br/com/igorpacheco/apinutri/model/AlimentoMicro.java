package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "alimento_micros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AlimentoMicro {
    @EmbeddedId
    private AlimentoMicroId id;

    @ManyToOne
    @MapsId("idAlimento")
    @JoinColumn(name = "id_alimento")
    private Alimento alimento;

    @ManyToOne
    @MapsId("idMicros")
    @JoinColumn(name = "id_micros")
    private MicroNutriente microNutriente;

    @Column(name = "gramas_por_100g")
    private BigDecimal gramasPor100g;

}
