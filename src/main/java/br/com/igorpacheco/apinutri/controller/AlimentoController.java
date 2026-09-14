package br.com.igorpacheco.apinutri.controller;

import br.com.igorpacheco.apinutri.dto.AlimentoResumoDTO;
import br.com.igorpacheco.apinutri.dto.CalculoRequestDTO;
import br.com.igorpacheco.apinutri.service.AlimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alimentos")
@RequiredArgsConstructor

public class AlimentoController {
    private final AlimentoService alimentoService;

    @GetMapping
    public List<AlimentoResumoDTO> buscar(@RequestParam String nome) {
        return alimentoService.buscarPorNome(nome);
    }

    @PostMapping("/calculo")
    public CalculoRequestDTO calcular(@RequestBody CalculoRequestDTO request) {
        return alimentoService.calcular(request);
    }
}
