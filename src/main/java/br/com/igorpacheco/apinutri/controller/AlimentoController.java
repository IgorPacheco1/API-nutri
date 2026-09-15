package br.com.igorpacheco.apinutri.controller;

import br.com.igorpacheco.apinutri.dto.AlimentoResumoDTO;
import br.com.igorpacheco.apinutri.service.AlimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alimentos")
@RequiredArgsConstructor

@Tag(name = "Alimentos", description = "Busca de alimentos cadastrados")
public class AlimentoController {

    private final AlimentoService alimentoService;

    @GetMapping
    @Operation(summary = "Busca alimentos pelo nome",
            description = "Retorna alimentos cujo nome contém o texto informado, ignorando maiúsculas/minúsculas")
    public List<AlimentoResumoDTO> buscar(@RequestParam String nome) {
        return alimentoService.buscarPorNome(nome);
    }
}
