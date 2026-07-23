package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.dto.output.SocioCreatoDto;
import it.def.prolocobe.entity.Socio;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.exception.RisorsaGiaEsistenteException;
import it.def.prolocobe.exception.RisorsaNonTrovataException;
import it.def.prolocobe.mapper.SocioMapper;
import it.def.prolocobe.repository.SocioRepository;
import it.def.prolocobe.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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
    private SocioMapper socioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final GeneratorePasswordTemporanea generatorePasswordTemporanea = new GeneratorePasswordTemporanea();

    private SocioService socioService;

    @BeforeEach
    void setUp() {
        socioService = new SocioService(socioRepository, utenteRepository, socioMapper, passwordEncoder, generatorePasswordTemporanea);
    }

    @Test
    void create_generaPasswordTemporaneaEUtenteSospesoSeQuotaNonPagata() {
        CreaSocioDto inputDto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15), false, null);
        Socio entity = new Socio();
        Socio savedEntity = new Socio();
        savedEntity.setId(1L);
        DettaglioSocioDto savedDto = new DettaglioSocioDto(1L, "Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15), false, null, false);

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
    void create_utenteAttivoSeQuotaGiaPagata() {
        CreaSocioDto inputDto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15),
                true, LocalDate.of(2026, 1, 15));
        Socio entity = new Socio();

        when(utenteRepository.findByEmail(inputDto.email())).thenReturn(Optional.empty());
        when(socioMapper.toEntity(inputDto)).thenReturn(entity);
        when(passwordEncoder.encode(any())).thenReturn("hash-fittizio");
        when(socioRepository.save(entity)).thenReturn(entity);
        when(socioMapper.toDto(entity)).thenReturn(null);

        socioService.create(inputDto);

        assertTrue(entity.getUtente().isAttivo());
    }

    @Test
    void create_lanciaEccezioneSeEmailGiaUsata() {
        CreaSocioDto inputDto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15), false, null);
        when(utenteRepository.findByEmail(inputDto.email())).thenReturn(Optional.of(new Utente()));

        assertThrows(RisorsaGiaEsistenteException.class, () -> socioService.create(inputDto));
    }

    @Test
    void findAll_restituisceIlRisultatoMappatoDalRepository() {
        Socio socio = new Socio();
        List<DettaglioSocioDto> expected = List.of(
                new DettaglioSocioDto(1L, "Mario", "Rossi", null, null, null, LocalDate.of(2026, 1, 15), false, null, false)
        );

        when(socioRepository.findAll()).thenReturn(List.of(socio));
        when(socioMapper.toDtoList(any())).thenReturn(expected);

        List<DettaglioSocioDto> result = socioService.findAll();

        assertEquals(expected, result);
    }

    @Test
    void findById_restituisceIlSocioMappatoQuandoEsiste() {
        Socio socio = new Socio();
        socio.setId(1L);
        DettaglioSocioDto expected = new DettaglioSocioDto(1L, "Mario", "Rossi", null, null, null, LocalDate.of(2026, 1, 15), false, null, false);

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
    void attivaAccount_impostaQuotaPagataEAttivaUtente() {
        Socio socio = new Socio();
        socio.setId(1L);
        Utente utente = new Utente();
        utente.setAttivo(false);
        socio.setUtente(utente);

        DettaglioSocioDto expected = new DettaglioSocioDto(1L, "Mario", "Rossi", null, null, null,
                LocalDate.of(2026, 1, 15), true, LocalDate.of(2026, 2, 1), true);

        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(socioRepository.save(socio)).thenReturn(socio);
        when(socioMapper.toDto(socio)).thenReturn(expected);

        DettaglioSocioDto result = socioService.attivaAccount(1L, LocalDate.of(2026, 2, 1));

        assertEquals(expected, result);
        assertTrue(socio.isQuotaPagata());
        assertEquals(LocalDate.of(2026, 2, 1), socio.getDataUltimoPagamento());
        assertTrue(utente.isAttivo());
    }

    @Test
    void attivaAccount_lanciaEccezioneQuandoSocioNonEsiste() {
        when(socioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RisorsaNonTrovataException.class, () -> socioService.attivaAccount(99L, LocalDate.now()));
    }
}
