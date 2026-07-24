package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.CambiaPasswordDto;
import it.def.prolocobe.dto.input.CreaUtenteDto;
import it.def.prolocobe.dto.output.DettaglioUtenteDto;
import it.def.prolocobe.dto.output.UtentePasswordResettataDto;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.exception.CredenzialiNonValideException;
import it.def.prolocobe.exception.RisorsaGiaEsistenteException;
import it.def.prolocobe.exception.RisorsaNonTrovataException;
import it.def.prolocobe.mapper.UtenteMapper;
import it.def.prolocobe.repository.UtenteRepository;
import it.def.prolocobe.util.EmailUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final UtenteMapper utenteMapper;
    private final PasswordEncoder passwordEncoder;
    private final GeneratorePasswordTemporanea generatorePasswordTemporanea;

    public UtenteService(UtenteRepository utenteRepository, UtenteMapper utenteMapper, PasswordEncoder passwordEncoder,
                          GeneratorePasswordTemporanea generatorePasswordTemporanea) {
        this.utenteRepository = utenteRepository;
        this.utenteMapper = utenteMapper;
        this.passwordEncoder = passwordEncoder;
        this.generatorePasswordTemporanea = generatorePasswordTemporanea;
    }

    public DettaglioUtenteDto create(CreaUtenteDto utenteDto) {
        String email = EmailUtils.normalizza(utenteDto.email());

        if (utenteRepository.findByEmail(email).isPresent()) {
            throw new RisorsaGiaEsistenteException("Esiste già un utente con email " + email);
        }

        Utente utente = utenteMapper.toEntity(utenteDto);
        utente.setEmail(email);
        utente.setPasswordHash(passwordEncoder.encode(utenteDto.password()));

        return utenteMapper.toDto(utenteRepository.save(utente));
    }

    @Transactional(readOnly = true)
    public Page<DettaglioUtenteDto> findAll(Pageable pageable) {
        return utenteRepository.findAll(pageable).map(utenteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public DettaglioUtenteDto findById(Long id) {
        return utenteRepository.findById(id)
                .map(utenteMapper::toDto)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente con id " + id + " non trovato"));
    }

    public void cambiaPassword(Long id, CambiaPasswordDto cambiaPasswordDto) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente con id " + id + " non trovato"));

        if (!passwordEncoder.matches(cambiaPasswordDto.passwordAttuale(), utente.getPasswordHash())) {
            throw new CredenzialiNonValideException("Password attuale non corretta");
        }

        utente.setPasswordHash(passwordEncoder.encode(cambiaPasswordDto.nuovaPassword()));
        utente.setDeveCambiarePassword(false);
        utenteRepository.save(utente);
    }

    public DettaglioUtenteDto impostaStatoAttivo(Long id, boolean attivo) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente con id " + id + " non trovato"));

        utente.setAttivo(attivo);
        return utenteMapper.toDto(utenteRepository.save(utente));
    }

    public UtentePasswordResettataDto resetPassword(Long id) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente con id " + id + " non trovato"));

        String passwordTemporanea = generatorePasswordTemporanea.genera();
        utente.setPasswordHash(passwordEncoder.encode(passwordTemporanea));
        utente.setDeveCambiarePassword(true);

        Utente salvato = utenteRepository.save(utente);
        return new UtentePasswordResettataDto(utenteMapper.toDto(salvato), passwordTemporanea);
    }
}
