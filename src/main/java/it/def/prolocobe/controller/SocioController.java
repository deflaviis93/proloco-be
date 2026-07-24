package it.def.prolocobe.controller;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.input.CreaTesseramentoDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.dto.output.DettaglioTesseramentoDto;
import it.def.prolocobe.dto.output.SocioCreatoDto;
import it.def.prolocobe.service.SocioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/soci")
@PreAuthorize("hasAnyRole('ADMIN','GESTIONE_SOCI')")
public class SocioController {

    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    @GetMapping
    public Page<DettaglioSocioDto> list(@PageableDefault(size = 20, sort = "cognome") Pageable pageable) {
        return socioService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public DettaglioSocioDto get(@PathVariable Long id) {
        return socioService.findById(id);
    }

    @PostMapping
    public SocioCreatoDto create(@Valid @RequestBody CreaSocioDto socioDto) {
        return socioService.create(socioDto);
    }

    @GetMapping("/{id}/tesseramenti")
    public List<DettaglioTesseramentoDto> tesseramenti(@PathVariable Long id) {
        return socioService.findTesseramenti(id);
    }

    @PostMapping("/{id}/tesseramenti")
    public DettaglioSocioDto registraTesseramento(@PathVariable Long id, @Valid @RequestBody CreaTesseramentoDto tesseramentoDto) {
        return socioService.registraTesseramento(id, tesseramentoDto);
    }
}
