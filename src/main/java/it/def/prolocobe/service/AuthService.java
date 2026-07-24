package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.LoginDto;
import it.def.prolocobe.dto.output.LoginRispostaDto;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.exception.AccountNonAttivoException;
import it.def.prolocobe.exception.CredenzialiNonValideException;
import it.def.prolocobe.repository.SocioRepository;
import it.def.prolocobe.repository.UtenteRepository;
import it.def.prolocobe.security.JwtService;
import it.def.prolocobe.util.EmailUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
public class AuthService {

    private final UtenteRepository utenteRepository;
    private final SocioRepository socioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtenteRepository utenteRepository, SocioRepository socioRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utenteRepository = utenteRepository;
        this.socioRepository = socioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginRispostaDto login(LoginDto loginDto) {
        Utente utente = utenteRepository.findByEmail(EmailUtils.normalizza(loginDto.email()))
                .orElseThrow(() -> new CredenzialiNonValideException("Email o password non validi"));

        if (!passwordEncoder.matches(loginDto.password(), utente.getPasswordHash())) {
            throw new CredenzialiNonValideException("Email o password non validi");
        }

        if (!puoAccedere(utente)) {
            throw new AccountNonAttivoException("Account sospeso: quota associativa non ancora pagata");
        }

        String token = jwtService.genera(utente);
        return new LoginRispostaDto(token, jwtService.scadenza(token), utente.getRuoli(), utente.isDeveCambiarePassword());
    }

    private boolean puoAccedere(Utente utente) {
        // Un account manualmente sospeso (attivo = false) non accede mai, chiunque sia.
        if (!utente.isAttivo()) {
            return false;
        }

        // Se l'utente è collegato a un socio deve anche essere in regola con la quota
        // dell'anno corrente. Il controllo è calcolato al volo, così resta corretto anche
        // al cambio d'anno senza dover ricalcolare flag denormalizzati.
        int annoCorrente = Year.now().getValue();
        return socioRepository.findByUtenteId(utente.getId())
                .map(socio -> socio.getTesseramenti().stream().anyMatch(t -> t.getAnno() == annoCorrente))
                .orElse(true);
    }
}
