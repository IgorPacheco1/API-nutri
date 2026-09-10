package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;

@Entity
@Table(name = "macro_nutrientes")

public class MacroNutriente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nome_macros")
    private String nomeMacros;

}
