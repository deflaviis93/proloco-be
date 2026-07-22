package it.def.prolocobe.mapper;

import it.def.prolocobe.dto.SocioDto;
import it.def.prolocobe.entity.Socio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface SocioMapper {

    SocioDto toDto(Socio socio);

    List<SocioDto> toDtoList(List<Socio> soci);

    @Mapping(target = "utente", ignore = true)
    Socio toEntity(SocioDto socioDto);
}
