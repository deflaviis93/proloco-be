package it.def.prolocobe.service;

import it.def.prolocobe.dto.SocioDto;
import it.def.prolocobe.entity.Socio;
import it.def.prolocobe.mapper.SocioMapper;
import it.def.prolocobe.repository.SocioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SocioService {

    private final SocioRepository socioRepository;
    private final SocioMapper socioMapper;

    protected SocioService() {
        this(null, null);
    }

    @Inject
    public SocioService(SocioRepository socioRepository, SocioMapper socioMapper) {
        this.socioRepository = socioRepository;
        this.socioMapper = socioMapper;
    }

    public SocioDto create(SocioDto socioDto) {
        Socio socio = socioMapper.toEntity(socioDto);
        return socioMapper.toDto(socioRepository.save(socio));
    }

    public List<SocioDto> findAll() {
        return socioMapper.toDtoList(socioRepository.findAll());
    }
}
