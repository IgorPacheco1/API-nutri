package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "micro_nutrientes")
@Getter
@Setter

public class MicroNutriente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_micros")
    private Integer id;

    @Column(name = "nome_micros")
    private String nomeMicros;
}
