package it.def.prolocobe.mapper;

import it.def.prolocobe.dto.input.CreaUtenteDto;
import it.def.prolocobe.dto.output.DettaglioUtenteDto;
import it.def.prolocobe.entity.Utente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UtenteMapper {

    DettaglioUtenteDto toDto(Utente utente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "attivo", ignore = true)
    Utente toEntity(CreaUtenteDto utenteDto);
}
