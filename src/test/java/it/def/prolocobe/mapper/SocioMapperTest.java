package it.def.prolocobe.mapper;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.entity.Socio;
import it.def.prolocobe.entity.Tesseramento;
import it.def.prolocobe.entity.Utente;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SocioMapperTest {

    private final SocioMapper socioMapper = new SocioMapperImpl();

    @Test
    void toDto_mappaTuttiICampi() {
        Socio socio = new Socio();
        socio.setId(1L);
        socio.setNome("Mario");
        socio.setCognome("Rossi");
        socio.setDataNascita(LocalDate.of(1980, 1, 1));
        socio.setTelefono("3331234567");
        socio.setEmail("mario.rossi@example.com");
        socio.setDataIscrizione(LocalDate.of(2026, 1, 15));

        DettaglioSocioDto dto = socioMapper.toDto(socio);

        assertEquals(1L, dto.id());
        assertEquals("Mario", dto.nome());
        assertEquals("Rossi", dto.cognome());
        assertEquals(LocalDate.of(1980, 1, 1), dto.dataNascita());
        assertEquals("3331234567", dto.telefono());
        assertEquals("mario.rossi@example.com", dto.email());
        assertEquals(LocalDate.of(2026, 1, 15), dto.dataIscrizione());
    }

    @Test
    void toDto_accountAttivoFalseQuandoNessunUtenteCollegato() {
        Socio socio = new Socio();

        DettaglioSocioDto dto = socioMapper.toDto(socio);

        assertFalse(dto.accountAttivo());
    }

    @Test
    void toDto_accountAttivoRiflettePropostoUtente() {
        Socio socio = new Socio();
        Utente utente = new Utente();
        utente.setAttivo(true);
        socio.setUtente(utente);

        DettaglioSocioDto dto = socioMapper.toDto(socio);

        assertTrue(dto.accountAttivo());
    }

    @Test
    void toDto_inRegolaFalseSenzaTesseramenti() {
        Socio socio = new Socio();

        DettaglioSocioDto dto = socioMapper.toDto(socio);

        assertFalse(dto.inRegola());
    }

    @Test
    void toDto_inRegolaTrueSeTesseramentoAnnoCorrente() {
        Socio socio = new Socio();
        Tesseramento tesseramento = new Tesseramento();
        tesseramento.setAnno(Year.now().getValue());
        socio.getTesseramenti().add(tesseramento);

        DettaglioSocioDto dto = socioMapper.toDto(socio);

        assertTrue(dto.inRegola());
    }

    @Test
    void toDto_inRegolaFalseSeSoloTesseramentiPassati() {
        Socio socio = new Socio();
        Tesseramento tesseramento = new Tesseramento();
        tesseramento.setAnno(Year.now().getValue() - 1);
        socio.getTesseramenti().add(tesseramento);

        DettaglioSocioDto dto = socioMapper.toDto(socio);

        assertFalse(dto.inRegola());
    }

    @Test
    void toEntity_ignoraIdEUtente() {
        CreaSocioDto dto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15));

        Socio socio = socioMapper.toEntity(dto);

        assertNull(socio.getId());
        assertEquals("Mario", socio.getNome());
        assertEquals("Rossi", socio.getCognome());
        assertNull(socio.getUtente());
    }

    @Test
    void toDtoList_mappaOgniElemento() {
        Socio socio1 = new Socio();
        socio1.setNome("Mario");
        Socio socio2 = new Socio();
        socio2.setNome("Luigi");

        List<DettaglioSocioDto> dtos = socioMapper.toDtoList(List.of(socio1, socio2));

        assertEquals(2, dtos.size());
        assertEquals("Mario", dtos.get(0).nome());
        assertEquals("Luigi", dtos.get(1).nome());
    }
}
