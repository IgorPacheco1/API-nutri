package br.com.igorpacheco.apinutri.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AliementoMacro implements Serializable {

    @Column(name = "id_alimento")
    private Integer idAlimento;

    @Column(name = "id_macros")
    private Integer idMacros;
}
