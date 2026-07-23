package it.def.prolocobe.security;

import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.enums.RuoloUtente;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtService {

    private final SecretKey chiave;
    private final long scadenzaMinuti;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-minutes}") long scadenzaMinuti) {
        this.chiave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.scadenzaMinuti = scadenzaMinuti;
    }

    public String genera(Utente utente) {
        Instant adesso = Instant.now();
        Instant scadenza = adesso.plus(scadenzaMinuti, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(String.valueOf(utente.getId()))
                .claim("email", utente.getEmail())
                .claim("ruoli", utente.getRuoli().stream().map(Enum::name).toList())
                .claim("deveCambiarePassword", utente.isDeveCambiarePassword())
                .issuedAt(Date.from(adesso))
                .expiration(Date.from(scadenza))
                .signWith(chiave)
                .compact();
    }

    public Instant scadenza(String token) {
        return valida(token).getExpiration().toInstant();
    }

    public Claims valida(String token) {
        return Jwts.parser()
                .verifyWith(chiave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UtenteAutenticato autenticatoDaClaims(Claims claims) {
        Long id = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        @SuppressWarnings("unchecked")
        List<String> ruoliClaim = claims.get("ruoli", List.class);
        Set<RuoloUtente> ruoli = ruoliClaim.stream()
                .map(RuoloUtente::valueOf)
                .collect(Collectors.toSet());
        boolean deveCambiarePassword = Boolean.TRUE.equals(claims.get("deveCambiarePassword", Boolean.class));

        return new UtenteAutenticato(id, email, ruoli, deveCambiarePassword);
    }
}
