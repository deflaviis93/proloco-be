package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.dto.output.SocioCreatoDto;
import it.def.prolocobe.entity.Socio;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.enums.RuoloUtente;
import it.def.prolocobe.exception.RisorsaGiaEsistenteException;
import it.def.prolocobe.exception.RisorsaNonTrovataException;
import it.def.prolocobe.mapper.SocioMapper;
import it.def.prolocobe.repository.SocioRepository;
import it.def.prolocobe.repository.UtenteRepository;
import it.def.prolocobe.util.EmailUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class SocioService {

    private final SocioRepository socioRepository;
    private final UtenteRepository utenteRepository;
    private final SocioMapper socioMapper;
    private final PasswordEncoder passwordEncoder;
    private final GeneratorePasswordTemporanea generatorePasswordTemporanea;

    public SocioService(SocioRepository socioRepository, UtenteRepository utenteRepository,
                         SocioMapper socioMapper, PasswordEncoder passwordEncoder,
                         GeneratorePasswordTemporanea generatorePasswordTemporanea) {
        this.socioRepository = socioRepository;
        this.utenteRepository = utenteRepository;
        this.socioMapper = socioMapper;
        this.passwordEncoder = passwordEncoder;
        this.generatorePasswordTemporanea = generatorePasswordTemporanea;
    }

    public SocioCreatoDto create(CreaSocioDto socioDto) {
        String email = EmailUtils.normalizza(socioDto.email());

        if (utenteRepository.findByEmail(email).isPresent()) {
            throw new RisorsaGiaEsistenteException("Esiste già un account con email " + email);
        }

        Socio socio = socioMapper.toEntity(socioDto);
        socio.setEmail(email);

        String passwordTemporanea = generatorePasswordTemporanea.genera();

        Utente utente = new Utente();
        utente.setEmail(email);
        utente.setPasswordHash(passwordEncoder.encode(passwordTemporanea));
        utente.setRuoli(Set.of(RuoloUtente.SOCIO));
        utente.setAttivo(socioDto.quotaPagata());
        utente.setDeveCambiarePassword(true);
        socio.setUtente(utente);

        Socio salvato = socioRepository.save(socio);
        return new SocioCreatoDto(socioMapper.toDto(salvato), passwordTemporanea);
    }

    public List<DettaglioSocioDto> findAll() {
        return socioMapper.toDtoList(socioRepository.findAll());
    }

    public DettaglioSocioDto findById(Long id) {
        return socioRepository.findById(id)
                .map(socioMapper::toDto)
                .orElseThrow(() -> new RisorsaNonTrovataException("Socio con id " + id + " non trovato"));
    }

    public DettaglioSocioDto attivaAccount(Long id, LocalDate dataPagamento) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Socio con id " + id + " non trovato"));

        socio.setQuotaPagata(true);
        socio.setDataUltimoPagamento(dataPagamento);
        if (socio.getUtente() != null) {
            socio.getUtente().setAttivo(true);
        }

        return socioMapper.toDto(socioRepository.save(socio));
    }
}
