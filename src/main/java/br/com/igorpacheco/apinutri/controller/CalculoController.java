package br.com.igorpacheco.apinutri.controller;

import br.com.igorpacheco.apinutri.dto.CalculoRequestDTO;
import br.com.igorpacheco.apinutri.dto.CalculoResponseDTO;
import br.com.igorpacheco.apinutri.service.CalculoNutricionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculo")
@RequiredArgsConstructor
@Tag(name = "Cálculo Nutricional", description = "Cálculo de nutrientes com base em alimentos e porções")
public class CalculoController {

    private final CalculoNutricionalService calculoNutricionalService;

    @PostMapping
    @Operation(summary = "Calcula os nutrientes totais de uma refeição",
            description = "Recebe uma lista de alimentos com suas quantidades em gramas e devolve o total de calorias, "
                    + "macronutrientes (g) e micronutrientes (unidade em unidadesMicros). "
                    + "Lista vazia, item sem idAlimento ou quantidade <= 0 ou > 10000 devolvem 400.")
    public CalculoResponseDTO calcular(@Valid @RequestBody CalculoRequestDTO request) {
        return calculoNutricionalService.calcular(request);
    }
}
