package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.input.CreaTesseramentoDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.dto.output.DettaglioTesseramentoDto;
import it.def.prolocobe.dto.output.SocioCreatoDto;
import it.def.prolocobe.entity.Socio;
import it.def.prolocobe.entity.Tesseramento;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.exception.RisorsaGiaEsistenteException;
import it.def.prolocobe.exception.RisorsaNonTrovataException;
import it.def.prolocobe.mapper.SocioMapper;
import it.def.prolocobe.mapper.TesseramentoMapper;
import it.def.prolocobe.repository.SocioRepository;
import it.def.prolocobe.repository.TesseramentoRepository;
import it.def.prolocobe.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocioServiceTest {

    @Mock
    private SocioRepository socioRepository;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private TesseramentoRepository tesseramentoRepository;

    @Mock
    private SocioMapper socioMapper;

    @Mock
    private TesseramentoMapper tesseramentoMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final GeneratorePasswordTemporanea generatorePasswordTemporanea = new GeneratorePasswordTemporanea();

    private SocioService socioService;

    @BeforeEach
    void setUp() {
        socioService = new SocioService(socioRepository, utenteRepository, tesseramentoRepository,
                socioMapper, tesseramentoMapper, passwordEncoder, generatorePasswordTemporanea);
    }

    @Test
    void create_generaPasswordTemporaneaEUtenteSempreSospesoAllaCreazione() {
        CreaSocioDto inputDto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15));
        Socio entity = new Socio();
        Socio savedEntity = new Socio();
        savedEntity.setId(1L);
        DettaglioSocioDto savedDto = new DettaglioSocioDto(1L, "Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15), false, false);

        when(utenteRepository.findByEmail(inputDto.email())).thenReturn(Optional.empty());
        when(socioMapper.toEntity(inputDto)).thenReturn(entity);
        when(passwordEncoder.encode(any())).thenReturn("hash-fittizio");
        when(socioRepository.save(entity)).thenReturn(savedEntity);
        when(socioMapper.toDto(savedEntity)).thenReturn(savedDto);

        SocioCreatoDto result = socioService.create(inputDto);

        assertEquals(savedDto, result.socio());
        assertEquals(12, result.passwordTemporanea().length());
        Utente utenteCollegato = entity.getUtente();
        assertEquals("mario.rossi@example.com", utenteCollegato.getEmail());
        assertEquals("hash-fittizio", utenteCollegato.getPasswordHash());
        assertFalse(utenteCollegato.isAttivo());
        assertTrue(utenteCollegato.isDeveCambiarePassword());
        verify(socioRepository).save(entity);
    }

    @Test
    void create_normalizzaEmailInMinuscoloPerControlloESalvataggio() {
        CreaSocioDto inputDto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", " Mario.Rossi@Example.COM ", LocalDate.of(2026, 1, 15));
        Socio entity = new Socio();

        when(utenteRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.empty());
        when(socioMapper.toEntity(inputDto)).thenReturn(entity);
        when(passwordEncoder.encode(any())).thenReturn("hash-fittizio");
        when(socioRepository.save(entity)).thenReturn(entity);
        when(socioMapper.toDto(entity)).thenReturn(null);

        socioService.create(inputDto);

        assertEquals("mario.rossi@example.com", entity.getEmail());
        assertEquals("mario.rossi@example.com", entity.getUtente().getEmail());
    }

    @Test
    void create_lanciaEccezioneSeEmailGiaUsata() {
        CreaSocioDto inputDto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15));
        when(utenteRepository.findByEmail(inputDto.email())).thenReturn(Optional.of(new Utente()));

        assertThrows(RisorsaGiaEsistenteException.class, () -> socioService.create(inputDto));
    }

    @Test
    void findAll_restituisceLaPaginaMappataDalRepository() {
        Socio socio = new Socio();
        DettaglioSocioDto expectedDto = new DettaglioSocioDto(1L, "Mario", "Rossi", null, null, null, LocalDate.of(2026, 1, 15), false, false);
        Pageable pageable = PageRequest.of(0, 20);

        when(socioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(socio)));
        when(socioMapper.toDto(socio)).thenReturn(expectedDto);

        Page<DettaglioSocioDto> result = socioService.findAll(pageable);

        assertEquals(List.of(expectedDto), result.getContent());
    }

    @Test
    void findById_restituisceIlSocioMappatoQuandoEsiste() {
        Socio socio = new Socio();
        socio.setId(1L);
        DettaglioSocioDto expected = new DettaglioSocioDto(1L, "Mario", "Rossi", null, null, null, LocalDate.of(2026, 1, 15), false, false);

        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(socioMapper.toDto(socio)).thenReturn(expected);

        DettaglioSocioDto result = socioService.findById(1L);

        assertEquals(expected, result);
    }

    @Test
    void findById_lanciaEccezioneQuandoNonEsiste() {
        when(socioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RisorsaNonTrovataException.class, () -> socioService.findById(99L));
    }

    @Test
    void registraTesseramento_attivaAccountSeAnnoCorrenteRegistrato() {
        Socio socio = new Socio();
        socio.setId(1L);
        Utente utente = new Utente();
        utente.setAttivo(false);
        socio.setUtente(utente);

        int annoCorrente = Year.now().getValue();
        CreaTesseramentoDto tesseramentoDto = new CreaTesseramentoDto(annoCorrente, new BigDecimal("15.00"), LocalDate.now());

        DettaglioSocioDto expected = new DettaglioSocioDto(1L, "Mario", "Rossi", null, null, null,
                LocalDate.of(2026, 1, 15), true, true);

        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(socioRepository.save(socio)).thenReturn(socio);
        when(socioMapper.toDto(socio)).thenReturn(expected);

        DettaglioSocioDto result = socioService.registraTesseramento(1L, tesseramentoDto);

        assertEquals(expected, result);
        assertEquals(1, socio.getTesseramenti().size());
        assertTrue(utente.isAttivo());
    }

    @Test
    void registraTesseramento_nonAttivaAccountSeAnnoRegistratoNonÈQuelloCorrente() {
        Socio socio = new Socio();
        socio.setId(1L);
        Utente utente = new Utente();
        utente.setAttivo(false);
        socio.setUtente(utente);

        CreaTesseramentoDto tesseramentoDto = new CreaTesseramentoDto(2019, new BigDecimal("15.00"), LocalDate.of(2019, 3, 1));

        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(socioRepository.save(socio)).thenReturn(socio);
        when(socioMapper.toDto(socio)).thenReturn(null);

        socioService.registraTesseramento(1L, tesseramentoDto);

        assertFalse(utente.isAttivo());
    }

    @Test
    void registraTesseramento_lanciaEccezioneSeAnnoGiaRegistrato() {
        Socio socio = new Socio();
        socio.setId(1L);
        Tesseramento esistente = new Tesseramento();
        esistente.setAnno(2026);
        socio.getTesseramenti().add(esistente);

        CreaTesseramentoDto tesseramentoDto = new CreaTesseramentoDto(2026, new BigDecimal("15.00"), LocalDate.now());

        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));

        assertThrows(RisorsaGiaEsistenteException.class, () -> socioService.registraTesseramento(1L, tesseramentoDto));
    }

    @Test
    void registraTesseramento_lanciaEccezioneQuandoSocioNonEsiste() {
        CreaTesseramentoDto tesseramentoDto = new CreaTesseramentoDto(2026, new BigDecimal("15.00"), LocalDate.now());
        when(socioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RisorsaNonTrovataException.class, () -> socioService.registraTesseramento(99L, tesseramentoDto));
    }

    @Test
    void findTesseramenti_restituisceLoStoricoOrdinato() {
        List<DettaglioTesseramentoDto> expected = List.of(
                new DettaglioTesseramentoDto(2L, 2026, new BigDecimal("15.00"), LocalDate.of(2026, 1, 10)),
                new DettaglioTesseramentoDto(1L, 2025, new BigDecimal("15.00"), LocalDate.of(2025, 1, 10))
        );

        when(socioRepository.existsById(1L)).thenReturn(true);
        when(tesseramentoRepository.findBySocioIdOrderByAnnoDesc(1L)).thenReturn(List.of());
        when(tesseramentoMapper.toDtoList(any())).thenReturn(expected);

        List<DettaglioTesseramentoDto> result = socioService.findTesseramenti(1L);

        assertEquals(expected, result);
    }

    @Test
    void findTesseramenti_lanciaEccezioneQuandoSocioNonEsiste() {
        when(socioRepository.existsById(99L)).thenReturn(false);

        assertThrows(RisorsaNonTrovataException.class, () -> socioService.findTesseramenti(99L));
    }
}
