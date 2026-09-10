package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "macro_nutrientes")
@Getter
@Setter
public class MacroNutriente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_macros")
    private int id;

    @Column(name = "nome_macros")
    private String nomeMacros;

}
