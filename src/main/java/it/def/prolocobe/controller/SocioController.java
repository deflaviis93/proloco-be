package it.def.prolocobe.controller;

import it.def.prolocobe.dto.input.AttivaAccountDto;
import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.dto.output.SocioCreatoDto;
import it.def.prolocobe.service.SocioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    public List<DettaglioSocioDto> list() {
        return socioService.findAll();
    }

    @GetMapping("/{id}")
    public DettaglioSocioDto get(@PathVariable Long id) {
        return socioService.findById(id);
    }

    @PostMapping
    public SocioCreatoDto create(@Valid @RequestBody CreaSocioDto socioDto) {
        return socioService.create(socioDto);
    }

    @PatchMapping("/{id}/attiva")
    public DettaglioSocioDto attiva(@PathVariable Long id, @Valid @RequestBody AttivaAccountDto attivaAccountDto) {
        return socioService.attivaAccount(id, attivaAccountDto.dataPagamento());
    }
}
