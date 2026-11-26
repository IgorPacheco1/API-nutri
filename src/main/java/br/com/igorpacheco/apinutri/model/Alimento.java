package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "alimentos")
@Getter
@Setter
public class Alimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alimento")
    private int idAlimento;

    @Column(name = "nome_alimento")
    private String nomeAlimento;

    @Column(name = "calorias_por_100g")
    private BigDecimal caloriasPor100g;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
}
