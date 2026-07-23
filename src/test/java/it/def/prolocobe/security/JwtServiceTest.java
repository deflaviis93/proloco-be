package it.def.prolocobe.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.enums.RuoloUtente;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-secret-lungo-almeno-32-caratteri-per-hs256";

    private final JwtService jwtService = new JwtService(SECRET, 480);

    @Test
    void genera_produceUnTokenValidabileConIClaimCorretti() {
        Utente utente = new Utente();
        utente.setId(42L);
        utente.setEmail("mario.rossi@example.com");
        utente.setRuoli(Set.of(RuoloUtente.SOCIO, RuoloUtente.GESTIONE_SOCI));
        utente.setDeveCambiarePassword(true);

        String token = jwtService.genera(utente);
        Claims claims = jwtService.valida(token);

        assertEquals("42", claims.getSubject());
        assertEquals("mario.rossi@example.com", claims.get("email", String.class));
        assertEquals(Boolean.TRUE, claims.get("deveCambiarePassword", Boolean.class));

        UtenteAutenticato utenteAutenticato = jwtService.autenticatoDaClaims(claims);
        assertEquals(42L, utenteAutenticato.id());
        assertEquals(Set.of(RuoloUtente.SOCIO, RuoloUtente.GESTIONE_SOCI), utenteAutenticato.ruoli());
        assertTrue(utenteAutenticato.deveCambiarePassword());
    }

    @Test
    void scadenza_ecircaOttoOreNelFuturo() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setEmail("mario.rossi@example.com");
        utente.setRuoli(Set.of(RuoloUtente.SOCIO));

        String token = jwtService.genera(utente);
        Instant scadenza = jwtService.scadenza(token);

        Instant atteso = Instant.now().plus(8, ChronoUnit.HOURS);
        assertTrue(Math.abs(scadenza.getEpochSecond() - atteso.getEpochSecond()) < 5);
    }

    @Test
    void valida_lanciaEccezioneSeTokenManomesso() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setEmail("mario.rossi@example.com");
        utente.setRuoli(Set.of(RuoloUtente.SOCIO));

        String token = jwtService.genera(utente);
        String tokenManomesso = token.substring(0, token.length() - 2) + "xx";

        assertThrows(JwtException.class, () -> jwtService.valida(tokenManomesso));
    }

    @Test
    void valida_lanciaEccezioneSeTokenScaduto() {
        JwtService jwtServiceScadenzaImmediata = new JwtService(SECRET, 0);
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setEmail("mario.rossi@example.com");
        utente.setRuoli(Set.of(RuoloUtente.SOCIO));

        String token = jwtServiceScadenzaImmediata.genera(utente);

        assertThrows(ExpiredJwtException.class, () -> jwtServiceScadenzaImmediata.valida(token));
    }
}
