package it.def.prolocobe.controller;

import it.def.prolocobe.dto.SocioDto;
import it.def.prolocobe.service.SocioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/soci")
public class SocioController {

    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    @GetMapping
    public List<SocioDto> list() {
        return socioService.findAll();
    }

    @PostMapping
    public SocioDto create(@RequestBody SocioDto socioDto) {
        return socioService.create(socioDto);
    }
}
