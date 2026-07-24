package it.def.prolocobe.mapper;

import it.def.prolocobe.dto.input.AggiornaSocioDto;
import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.entity.Socio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SocioMapper {

    @Mapping(target = "utenteId", expression = "java(socio.getUtente() != null ? socio.getUtente().getId() : null)")
    @Mapping(target = "accountAttivo", expression = "java(socio.getUtente() != null && socio.getUtente().isAttivo())")
    @Mapping(target = "inRegola", expression = "java(socio.getTesseramenti().stream().anyMatch(t -> t.getAnno() == java.time.Year.now().getValue()))")
    DettaglioSocioDto toDto(Socio socio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utente", ignore = true)
    @Mapping(target = "tesseramenti", ignore = true)
    Socio toEntity(CreaSocioDto socioDto);

    // L'email viene sincronizzata a mano nel service (deve restare allineata con l'account
    // di login e verificata per unicità), quindi qui la ignoriamo.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utente", ignore = true)
    @Mapping(target = "tesseramenti", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateEntity(AggiornaSocioDto socioDto, @MappingTarget Socio socio);
}
