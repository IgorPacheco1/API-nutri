package br.com.igorpacheco.apinutri.controller;

import br.com.igorpacheco.apinutri.dto.CalculoRequestDTO;
import br.com.igorpacheco.apinutri.dto.CalculoResponseDTO;
import br.com.igorpacheco.apinutri.service.CalculoNutricionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculo")
@RequiredArgsConstructor
public class CalculoController {
    private final CalculoNutricionalService calculoNutricionalService;

    @PostMapping
    public CalculoResponseDTO calcular(@RequestBody CalculoRequestDTO request) {
        return calculoNutricionalService.calcular(request);
    }
}
