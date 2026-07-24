package it.def.prolocobe.controller;

import it.def.prolocobe.dto.input.AggiornaSocioDto;
import it.def.prolocobe.dto.input.AggiornaStatoDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/soci")
public class SocioController {

    // Le operazioni di scrittura sono riservate a chi gestisce l'anagrafica.
    private static final String GESTORI = "hasAnyRole('ADMIN','GESTIONE_SOCI')";

    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    // --- Lettura: consentita a qualsiasi utente autenticato (anche un SOCIO) ---

    @GetMapping
    public Page<DettaglioSocioDto> list(@PageableDefault(size = 20, sort = "cognome") Pageable pageable) {
        return socioService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public DettaglioSocioDto get(@PathVariable Long id) {
        return socioService.findById(id);
    }

    @GetMapping("/{id}/tesseramenti")
    public List<DettaglioTesseramentoDto> tesseramenti(@PathVariable Long id) {
        return socioService.findTesseramenti(id);
    }

    // --- Scrittura: solo ADMIN / GESTIONE_SOCI ---

    @PostMapping
    @PreAuthorize(GESTORI)
    @ResponseStatus(HttpStatus.CREATED)
    public SocioCreatoDto create(@Valid @RequestBody CreaSocioDto socioDto) {
        return socioService.create(socioDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize(GESTORI)
    public DettaglioSocioDto update(@PathVariable Long id, @Valid @RequestBody AggiornaSocioDto socioDto) {
        return socioService.update(id, socioDto);
    }

    @PatchMapping("/{id}/stato")
    @PreAuthorize(GESTORI)
    public DettaglioSocioDto impostaStato(@PathVariable Long id, @Valid @RequestBody AggiornaStatoDto statoDto) {
        return socioService.impostaStatoAttivo(id, statoDto.attivo());
    }

    @PostMapping("/{id}/tesseramenti")
    @PreAuthorize(GESTORI)
    @ResponseStatus(HttpStatus.CREATED)
    public DettaglioSocioDto registraTesseramento(@PathVariable Long id, @Valid @RequestBody CreaTesseramentoDto tesseramentoDto) {
        return socioService.registraTesseramento(id, tesseramentoDto);
    }
}
