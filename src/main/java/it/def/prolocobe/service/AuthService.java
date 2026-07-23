package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.LoginDto;
import it.def.prolocobe.dto.output.LoginRispostaDto;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.exception.AccountNonAttivoException;
import it.def.prolocobe.exception.CredenzialiNonValideException;
import it.def.prolocobe.repository.UtenteRepository;
import it.def.prolocobe.security.JwtService;
import it.def.prolocobe.util.EmailUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginRispostaDto login(LoginDto loginDto) {
        Utente utente = utenteRepository.findByEmail(EmailUtils.normalizza(loginDto.email()))
                .orElseThrow(() -> new CredenzialiNonValideException("Email o password non validi"));

        if (!passwordEncoder.matches(loginDto.password(), utente.getPasswordHash())) {
            throw new CredenzialiNonValideException("Email o password non validi");
        }

        if (!utente.isAttivo()) {
            throw new AccountNonAttivoException("Account sospeso: quota associativa non ancora pagata");
        }

        String token = jwtService.genera(utente);
        return new LoginRispostaDto(token, jwtService.scadenza(token), utente.getRuoli(), utente.isDeveCambiarePassword());
    }
}
