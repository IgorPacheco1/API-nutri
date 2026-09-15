package br.com.igorpacheco.apinutri.controller;

import br.com.igorpacheco.apinutri.dto.AlimentoResumoDTO;
import br.com.igorpacheco.apinutri.service.AlimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
