package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alimento")

public class Alimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_micros")
    private int idMicros;

    @Column(name = "nome_alimento")
    private String nomeAlimento;

    @Column(name = "calorias_por_100g")
    private double caloriasPor100g;
}
