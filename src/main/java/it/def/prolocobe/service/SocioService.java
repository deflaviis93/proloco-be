package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.CreaSocioDto;
import it.def.prolocobe.dto.input.CreaTesseramentoDto;
import it.def.prolocobe.dto.output.DettaglioSocioDto;
import it.def.prolocobe.dto.output.DettaglioTesseramentoDto;
import it.def.prolocobe.dto.output.SocioCreatoDto;
import it.def.prolocobe.entity.Socio;
import it.def.prolocobe.entity.Tesseramento;
import it.def.prolocobe.entity.Utente;
import it.def.prolocobe.enums.RuoloUtente;
import it.def.prolocobe.exception.RisorsaGiaEsistenteException;
import it.def.prolocobe.exception.RisorsaNonTrovataException;
import it.def.prolocobe.mapper.SocioMapper;
import it.def.prolocobe.mapper.TesseramentoMapper;
import it.def.prolocobe.repository.SocioRepository;
import it.def.prolocobe.repository.TesseramentoRepository;
import it.def.prolocobe.repository.UtenteRepository;
import it.def.prolocobe.util.EmailUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class SocioService {

    private final SocioRepository socioRepository;
    private final UtenteRepository utenteRepository;
    private final TesseramentoRepository tesseramentoRepository;
    private final SocioMapper socioMapper;
    private final TesseramentoMapper tesseramentoMapper;
    private final PasswordEncoder passwordEncoder;
    private final GeneratorePasswordTemporanea generatorePasswordTemporanea;

    public SocioService(SocioRepository socioRepository, UtenteRepository utenteRepository,
                         TesseramentoRepository tesseramentoRepository, SocioMapper socioMapper,
                         TesseramentoMapper tesseramentoMapper, PasswordEncoder passwordEncoder,
                         GeneratorePasswordTemporanea generatorePasswordTemporanea) {
        this.socioRepository = socioRepository;
        this.utenteRepository = utenteRepository;
        this.tesseramentoRepository = tesseramentoRepository;
        this.socioMapper = socioMapper;
        this.tesseramentoMapper = tesseramentoMapper;
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
        utente.setAttivo(false);
        utente.setDeveCambiarePassword(true);
        socio.setUtente(utente);

        Socio salvato = socioRepository.save(socio);
        return new SocioCreatoDto(socioMapper.toDto(salvato), passwordTemporanea);
    }

    @Transactional(readOnly = true)
    public List<DettaglioSocioDto> findAll() {
        return socioMapper.toDtoList(socioRepository.findAll());
    }

    @Transactional(readOnly = true)
    public DettaglioSocioDto findById(Long id) {
        return socioRepository.findById(id)
                .map(socioMapper::toDto)
                .orElseThrow(() -> new RisorsaNonTrovataException("Socio con id " + id + " non trovato"));
    }

    public DettaglioSocioDto registraTesseramento(Long id, CreaTesseramentoDto tesseramentoDto) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Socio con id " + id + " non trovato"));

        boolean annoGiaRegistrato = socio.getTesseramenti().stream()
                .anyMatch(t -> t.getAnno() == tesseramentoDto.anno());
        if (annoGiaRegistrato) {
            throw new RisorsaGiaEsistenteException(
                    "Esiste già un tesseramento per l'anno " + tesseramentoDto.anno() + " per questo socio");
        }

        Tesseramento tesseramento = new Tesseramento();
        tesseramento.setSocio(socio);
        tesseramento.setAnno(tesseramentoDto.anno());
        tesseramento.setImporto(tesseramentoDto.importo());
        tesseramento.setDataPagamento(tesseramentoDto.dataPagamento());
        socio.getTesseramenti().add(tesseramento);

        int annoCorrente = Year.now().getValue();
        boolean inRegola = socio.getTesseramenti().stream().anyMatch(t -> t.getAnno() == annoCorrente);
        if (socio.getUtente() != null) {
            socio.getUtente().setAttivo(inRegola);
        }

        return socioMapper.toDto(socioRepository.save(socio));
    }

    @Transactional(readOnly = true)
    public List<DettaglioTesseramentoDto> findTesseramenti(Long id) {
        if (!socioRepository.existsById(id)) {
            throw new RisorsaNonTrovataException("Socio con id " + id + " non trovato");
        }
        return tesseramentoMapper.toDtoList(tesseramentoRepository.findBySocioIdOrderByAnnoDesc(id));
    }
}
