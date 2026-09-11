package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.jmx.export.annotation.ManagedNotification;

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
    @MapsId("idAlimentos")
    @JoinColumn(name = "id_alimentos")
    private Alimento alimento;

    @ManyToOne
    @MapsId("idMicros")
    @JoinColumn(name = "id_micros")
    private MicroNutriente microNutriente;

    @Column(name = "gramas_por_100g")
    private Double gramasPor100g;

}
