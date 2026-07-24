package it.def.prolocobe.service;

import it.def.prolocobe.dto.input.AggiornaSocioDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        // Se la quota è pagata già all'iscrizione, si registra subito il primo tesseramento;
        // se è per l'anno corrente l'account nasce già attivo (e quindi in regola).
        if (socioDto.tesseramento() != null) {
            CreaTesseramentoDto tesseramentoDto = socioDto.tesseramento();
            Tesseramento tesseramento = new Tesseramento();
            tesseramento.setSocio(socio);
            tesseramento.setAnno(tesseramentoDto.anno());
            tesseramento.setImporto(tesseramentoDto.importo());
            tesseramento.setDataPagamento(tesseramentoDto.dataPagamento());
            socio.getTesseramenti().add(tesseramento);

            if (tesseramentoDto.anno() == Year.now().getValue()) {
                utente.setAttivo(true);
            }
        }

        Socio salvato = socioRepository.save(socio);
        return new SocioCreatoDto(socioMapper.toDto(salvato), passwordTemporanea);
    }

    @Transactional(readOnly = true)
    public Page<DettaglioSocioDto> findAll(Pageable pageable) {
        return socioRepository.findAll(pageable).map(socioMapper::toDto);
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
        // Il pagamento della quota corrente riattiva l'account, ma non lo sospende mai:
        // la sospensione manuale resta di competenza dell'admin e il mancato rinnovo è
        // già gestito al login dal controllo di regolarità (calcolato al volo).
        if (inRegola && socio.getUtente() != null) {
            socio.getUtente().setAttivo(true);
        }

        return socioMapper.toDto(socioRepository.save(socio));
    }

    public DettaglioSocioDto update(Long id, AggiornaSocioDto socioDto) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Socio con id " + id + " non trovato"));

        String nuovaEmail = EmailUtils.normalizza(socioDto.email());
        Utente utente = socio.getUtente();

        // Se l'email cambia va tenuta allineata con l'account di login e resta unica.
        if (utente != null && !nuovaEmail.equals(utente.getEmail())) {
            utenteRepository.findByEmail(nuovaEmail)
                    .filter(altro -> !altro.getId().equals(utente.getId()))
                    .ifPresent(altro -> {
                        throw new RisorsaGiaEsistenteException("Esiste già un account con email " + nuovaEmail);
                    });
            utente.setEmail(nuovaEmail);
        }

        socioMapper.updateEntity(socioDto, socio);
        socio.setEmail(nuovaEmail);

        return socioMapper.toDto(socioRepository.save(socio));
    }

    public DettaglioSocioDto impostaStatoAttivo(Long id, boolean attivo) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Socio con id " + id + " non trovato"));

        if (socio.getUtente() == null) {
            throw new RisorsaNonTrovataException("Il socio con id " + id + " non ha un account collegato");
        }

        socio.getUtente().setAttivo(attivo);
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
