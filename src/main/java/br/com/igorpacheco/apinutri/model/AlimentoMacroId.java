package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AlimentoMacroId implements Serializable {

    @Column(name = "id_alimento")
    private Integer idAlimento;

    @Column(name = "id_macros")
    private Integer idMacros;

}
