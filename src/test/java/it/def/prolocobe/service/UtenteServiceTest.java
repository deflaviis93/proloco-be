package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.CambiaPasswordDto;
import it.def.prolocobe.dto.input.CreaUtenteDto;
import it.def.prolocobe.dto.output.DettaglioUtenteDto;
import it.def.prolocobe.dto.output.UtentePasswordResettataDto;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.enums.RuoloUtente;
import it.def.prolocobe.exception.CredenzialiNonValideException;
import it.def.prolocobe.exception.RisorsaGiaEsistenteException;
import it.def.prolocobe.exception.RisorsaNonTrovataException;
import it.def.prolocobe.mapper.UtenteMapper;
import it.def.prolocobe.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtenteServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private UtenteMapper utenteMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final GeneratorePasswordTemporanea generatorePasswordTemporanea = new GeneratorePasswordTemporanea();

    private UtenteService utenteService;

    @BeforeEach
    void setUp() {
        utenteService = new UtenteService(utenteRepository, utenteMapper, passwordEncoder, generatorePasswordTemporanea);
    }

    @Test
    void create_hashaLaPasswordESalva() {
        CreaUtenteDto inputDto = new CreaUtenteDto("mario.rossi@example.com", "password123", Set.of(RuoloUtente.GESTIONE_SOCI));
        Utente entity = new Utente();
        Utente savedEntity = new Utente();
        savedEntity.setId(1L);
        DettaglioUtenteDto expected = new DettaglioUtenteDto(1L, "mario.rossi@example.com", Set.of(RuoloUtente.GESTIONE_SOCI), true, true);

        when(utenteRepository.findByEmail(inputDto.email())).thenReturn(Optional.empty());
        when(utenteMapper.toEntity(inputDto)).thenReturn(entity);
        when(passwordEncoder.encode("password123")).thenReturn("hash-fittizio");
        when(utenteRepository.save(entity)).thenReturn(savedEntity);
        when(utenteMapper.toDto(savedEntity)).thenReturn(expected);

        DettaglioUtenteDto result = utenteService.create(inputDto);

        assertEquals(expected, result);
        assertEquals("hash-fittizio", entity.getPasswordHash());
        verify(utenteRepository).save(entity);
    }

    @Test
    void create_normalizzaEmailInMinuscoloPerControlloESalvataggio() {
        CreaUtenteDto inputDto = new CreaUtenteDto(" Mario.Rossi@Example.COM ", "password123", Set.of(RuoloUtente.GESTIONE_SOCI));
        Utente entity = new Utente();

        when(utenteRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.empty());
        when(utenteMapper.toEntity(inputDto)).thenReturn(entity);
        when(passwordEncoder.encode("password123")).thenReturn("hash-fittizio");
        when(utenteRepository.save(entity)).thenReturn(entity);
        when(utenteMapper.toDto(entity)).thenReturn(null);

        utenteService.create(inputDto);

        assertEquals("mario.rossi@example.com", entity.getEmail());
    }

    @Test
    void create_lanciaEccezioneSeEmailGiaPresente() {
        CreaUtenteDto inputDto = new CreaUtenteDto("mario.rossi@example.com", "password123", Set.of(RuoloUtente.GESTIONE_SOCI));
        when(utenteRepository.findByEmail(inputDto.email())).thenReturn(Optional.of(new Utente()));

        assertThrows(RisorsaGiaEsistenteException.class, () -> utenteService.create(inputDto));
    }

    @Test
    void findById_lanciaEccezioneQuandoNonEsiste() {
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RisorsaNonTrovataException.class, () -> utenteService.findById(99L));
    }

    @Test
    void cambiaPassword_aggiornaHashEResettaFlagQuandoPasswordAttualeCorretta() {
        Utente utente = new Utente();
        utente.setPasswordHash("hash-vecchio");
        utente.setDeveCambiarePassword(true);
        CambiaPasswordDto dto = new CambiaPasswordDto("vecchiaPassword", "nuovaPassword123");

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("vecchiaPassword", "hash-vecchio")).thenReturn(true);
        when(passwordEncoder.encode("nuovaPassword123")).thenReturn("hash-nuovo");

        utenteService.cambiaPassword(1L, dto);

        assertEquals("hash-nuovo", utente.getPasswordHash());
        assertFalse(utente.isDeveCambiarePassword());
        verify(utenteRepository).save(utente);
    }

    @Test
    void cambiaPassword_lanciaEccezioneSePasswordAttualeErrata() {
        Utente utente = new Utente();
        utente.setPasswordHash("hash-vecchio");
        CambiaPasswordDto dto = new CambiaPasswordDto("passwordSbagliata", "nuovaPassword123");

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("passwordSbagliata", "hash-vecchio")).thenReturn(false);

        assertThrows(CredenzialiNonValideException.class, () -> utenteService.cambiaPassword(1L, dto));
    }

    @Test
    void cambiaPassword_lanciaEccezioneQuandoUtenteNonEsiste() {
        CambiaPasswordDto dto = new CambiaPasswordDto("qualsiasi", "nuovaPassword123");
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RisorsaNonTrovataException.class, () -> utenteService.cambiaPassword(99L, dto));
    }

    @Test
    void resetPassword_generaNuovaPasswordERimetteIlFlagDeveCambiare() {
        Utente utente = new Utente();
        utente.setDeveCambiarePassword(false);
        DettaglioUtenteDto expectedDto = new DettaglioUtenteDto(1L, "mario.rossi@example.com", Set.of(RuoloUtente.SOCIO), true, true);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(passwordEncoder.encode(any())).thenReturn("hash-nuovo");
        when(utenteRepository.save(utente)).thenReturn(utente);
        when(utenteMapper.toDto(utente)).thenReturn(expectedDto);

        UtentePasswordResettataDto result = utenteService.resetPassword(1L);

        assertEquals(expectedDto, result.utente());
        assertEquals(12, result.passwordTemporanea().length());
        assertEquals("hash-nuovo", utente.getPasswordHash());
        assertTrue(utente.isDeveCambiarePassword());
    }

    @Test
    void resetPassword_lanciaEccezioneQuandoUtenteNonEsiste() {
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RisorsaNonTrovataException.class, () -> utenteService.resetPassword(99L));
    }

    @Test
    void impostaStatoAttivo_aggiornaIlFlagESalva() {
        Utente utente = new Utente();
        utente.setAttivo(true);
        DettaglioUtenteDto expected = new DettaglioUtenteDto(1L, "mario.rossi@example.com", Set.of(RuoloUtente.SOCIO), false, false);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(utenteRepository.save(utente)).thenReturn(utente);
        when(utenteMapper.toDto(utente)).thenReturn(expected);

        DettaglioUtenteDto result = utenteService.impostaStatoAttivo(1L, false);

        assertEquals(expected, result);
        assertFalse(utente.isAttivo());
    }

    @Test
    void impostaStatoAttivo_lanciaEccezioneQuandoUtenteNonEsiste() {
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RisorsaNonTrovataException.class, () -> utenteService.impostaStatoAttivo(99L, true));
    }
}
