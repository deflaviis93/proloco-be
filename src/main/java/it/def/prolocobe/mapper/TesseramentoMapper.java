package it.def.prolocobe.mapper;

import it.def.prolocobe.dto.output.DettaglioTesseramentoDto;
import it.def.prolocobe.entity.Tesseramento;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TesseramentoMapper {

    DettaglioTesseramentoDto toDto(Tesseramento tesseramento);

    List<DettaglioTesseramentoDto> toDtoList(List<Tesseramento> tesseramenti);
}
