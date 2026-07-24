package it.def.prolocobe.controller;

import it.def.prolocobe.dto.input.CambiaPasswordDto;
import it.def.prolocobe.dto.input.CreaUtenteDto;
import it.def.prolocobe.dto.output.DettaglioUtenteDto;
import it.def.prolocobe.dto.output.UtentePasswordResettataDto;
import it.def.prolocobe.service.UtenteService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONE_SOCI')")
    public Page<DettaglioUtenteDto> list(@PageableDefault(size = 20, sort = "email") Pageable pageable) {
        return utenteService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONE_SOCI')")
    public DettaglioUtenteDto get(@PathVariable Long id) {
        return utenteService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONE_SOCI')")
    public DettaglioUtenteDto create(@Valid @RequestBody CreaUtenteDto utenteDto) {
        return utenteService.create(utenteDto);
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiaPassword(@PathVariable Long id, @Valid @RequestBody CambiaPasswordDto cambiaPasswordDto) {
        utenteService.cambiaPassword(id, cambiaPasswordDto);
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN','GESTIONE_SOCI')")
    public UtentePasswordResettataDto resetPassword(@PathVariable Long id) {
        return utenteService.resetPassword(id);
    }
}
