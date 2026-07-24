package it.def.prolocobe.mapper;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.entity.Socio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SocioMapper {

    @Mapping(target = "accountAttivo", expression = "java(socio.getUtente() != null && socio.getUtente().isAttivo())")
    @Mapping(target = "inRegola", expression = "java(socio.getTesseramenti().stream().anyMatch(t -> t.getAnno() == java.time.Year.now().getValue()))")
    DettaglioSocioDto toDto(Socio socio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utente", ignore = true)
    @Mapping(target = "tesseramenti", ignore = true)
    Socio toEntity(CreaSocioDto socioDto);
}
