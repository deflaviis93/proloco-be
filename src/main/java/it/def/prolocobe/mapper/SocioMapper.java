package it.def.prolocobe.mapper;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.entity.Socio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SocioMapper {

    @Mapping(target = "accountAttivo", expression = "java(socio.getUtente() != null && socio.getUtente().isAttivo())")
    DettaglioSocioDto toDto(Socio socio);

    List<DettaglioSocioDto> toDtoList(List<Socio> soci);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utente", ignore = true)
    Socio toEntity(CreaSocioDto socioDto);
}
