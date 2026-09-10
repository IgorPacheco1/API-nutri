package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;

@Entity
@Table(name = "micro_nutrientes")

public class MicroNutriente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_micros")
    private int id;

    @Column(name = "nome_micros")
    private String nomeMicros;
}
