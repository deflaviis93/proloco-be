# Proloco BE — Nerito

Backend per la gestione della Pro Loco del paese di Nerito.

## Stato del progetto

Fase 1 (gestione soci/utenti + autenticazione) implementata. Sono attivi:

- **Auth**: `POST /api/auth/login`. Logout gestito lato client (JWT stateless).
- **Soci** (`/api/soci`): lettura (`GET` lista, dettaglio, tesseramenti) aperta a qualsiasi
  utente autenticato; scrittura (`POST` nuovo, `PUT /{id}` aggiorna, `PATCH /{id}/stato`
  attiva/sospende, `POST /{id}/tesseramenti`) riservata a `ADMIN`/`GESTIONE_SOCI`.
- **Utenti** (`/api/utenti`): lista/dettaglio/creazione/reset-password per `ADMIN`/`GESTIONE_SOCI`,
  `PATCH /{id}/stato` (attiva/disattiva) solo `ADMIN`, `PATCH /{id}/password` per l'utente stesso o `ADMIN`.

Login gate (`AuthService.puoAccedere`): `Utente.attivo` è l'unica sorgente di verità
(sospensione manuale) e, per i soci, si aggiunge il controllo di regolarità con la quota
dell'anno corrente (calcolato al volo). Il pagamento di un tesseramento corrente riattiva
l'account ma non lo sospende mai automaticamente.

Da costruire: eventi/manifestazioni.

Nota: il progetto è partito su Jakarta EE puro + Open Liberty, poi migrato a Spring Boot
per ridurre l'attrito operativo (jar eseguibile invece di application server, deploy più
semplice) — scelta corretta per un progetto community-size senza vincoli enterprise.

## Scope funzionale (fase 1)

- **Gestione socie/tesserati**: anagrafica soci, storico tesseramenti (`Tesseramento`: anno, importo,
  data pagamento — un socio è "in regola" se ha un tesseramento per l'anno corrente, calcolato
  al volo, non un flag denormalizzato). Un socio senza tesseramento per l'anno in corso ha
  l'account sospeso (`Utente.attivo = false`), aggiornato automaticamente alla registrazione
  di un nuovo tesseramento.
- **Eventi e manifestazioni**: creazione eventi/sagre, programma, turni volontari, prenotazioni posti.

Fuori scope per ora (da valutare in fasi successive): contabilità/entrate-uscite, gestione
turni volontari granulare per stand/cucina/biglietteria.

## Stack tecnico

- **Linguaggio**: Java 21
- **Framework**: Spring Boot 3 (Spring MVC per le API REST, Spring Data JPA per la persistenza, Spring Validation per la validazione degli input)
- **Database**: PostgreSQL, provider JPA Hibernate (via Spring Data JPA)
- **Build**: Maven (wrapper incluso, `./mvnw`), packaging jar eseguibile (`spring-boot-maven-plugin`)
- **Deploy**: Docker — immagine multi-stage (build Maven → runtime `eclipse-temurin:21-jre`), orchestrata con `docker-compose.yml` insieme al container Postgres
- **Autenticazione**: JWT stateless via Spring Security. Login su `POST /api/auth/login` (email + password), token HS256 valido 8 ore con claim `ruoli` e `deveCambiarePassword`. Endpoint protetti con `@PreAuthorize` in base al ruolo (`ADMIN`, `SOCIO`, `VOLONTARIO`, `GESTIONE_SOCI`). Nessuna registrazione pubblica: solo chi ha ruolo `ADMIN`/`GESTIONE_SOCI` crea nuovi soci/utenti. Il primo account `ADMIN` va creato manualmente via SQL (nessun bootstrap applicativo) — vedi `security/JwtService.java` e `config/SecurityConfig.java`.

## Architettura

- Solo API REST, nessun rendering server-side. Il frontend (SPA/mobile) è un consumer separato.
- Package base: `it.def.prolocobe`

## Repository

- Repo Git locale inizializzato, branch principale `main`.
- Destinazione: GitHub (da collegare — `gh` CLI non presente sulla macchina di sviluppo).

## Prossimi passi

1. Modellare eventi/manifestazioni (entità JPA, turni volontari, prenotazioni posti).
2. Collegare il repository a GitHub e impostare CI di base (build + test).
3. Valutare test di integrazione (MockMvc + Testcontainers) per il flusso di autenticazione end-to-end.
