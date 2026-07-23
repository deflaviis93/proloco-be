package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.LoginDto;
import it.def.prolocobe.dto.output.LoginRispostaDto;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.enums.RuoloUtente;
import it.def.prolocobe.exception.AccountNonAttivoException;
import it.def.prolocobe.exception.CredenzialiNonValideException;
import it.def.prolocobe.repository.UtenteRepository;
import it.def.prolocobe.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final JwtService jwtService = new JwtService("test-secret-lungo-almeno-32-caratteri-per-hs256", 480);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(utenteRepository, passwordEncoder, jwtService);
    }

    @Test
    void login_restituisceTokenQuandoCredenzialiValideEAccountAttivo() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setEmail("mario.rossi@example.com");
        utente.setPasswordHash("hash");
        utente.setRuoli(Set.of(RuoloUtente.SOCIO));
        utente.setAttivo(true);
        utente.setDeveCambiarePassword(false);
        LoginDto loginDto = new LoginDto("mario.rossi@example.com", "password123");

        when(utenteRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        LoginRispostaDto result = authService.login(loginDto);

        assertFalse(result.token().isBlank());
        assertTrue(result.scadenza().isAfter(Instant.now()));
        assertEquals(Set.of(RuoloUtente.SOCIO), result.ruoli());
        assertEquals(false, result.deveCambiarePassword());
    }

    @Test
    void login_lanciaEccezioneSeEmailNonEsiste() {
        LoginDto loginDto = new LoginDto("sconosciuto@example.com", "password123");
        when(utenteRepository.findByEmail("sconosciuto@example.com")).thenReturn(Optional.empty());

        assertThrows(CredenzialiNonValideException.class, () -> authService.login(loginDto));
    }

    @Test
    void login_lanciaEccezioneSePasswordErrata() {
        Utente utente = new Utente();
        utente.setPasswordHash("hash");
        LoginDto loginDto = new LoginDto("mario.rossi@example.com", "passwordSbagliata");

        when(utenteRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("passwordSbagliata", "hash")).thenReturn(false);

        assertThrows(CredenzialiNonValideException.class, () -> authService.login(loginDto));
    }

    @Test
    void login_lanciaEccezioneSeAccountNonAttivo() {
        Utente utente = new Utente();
        utente.setPasswordHash("hash");
        utente.setAttivo(false);
        LoginDto loginDto = new LoginDto("mario.rossi@example.com", "password123");

        when(utenteRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        assertThrows(AccountNonAttivoException.class, () -> authService.login(loginDto));
    }
}
