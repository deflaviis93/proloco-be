# Proloco BE — Nerito

Backend per la gestione della Pro Loco del paese di Nerito.

## Stato del progetto

Progetto nuovo, in fase di avvio. Lo scheletro tecnico esiste (build Maven, deploy Liberty),
il dominio applicativo (entità, endpoint, DB) è ancora da costruire.

## Scope funzionale (fase 1)

- **Gestione socie/tesserati**: anagrafica soci, tessere, quote associative, scadenze di rinnovo.
- **Eventi e manifestazioni**: creazione eventi/sagre, programma, turni volontari, prenotazioni posti.

Fuori scope per ora (da valutare in fasi successive): contabilità/entrate-uscite, gestione
turni volontari granulare per stand/cucina/biglietteria.

## Stack tecnico

- **Linguaggio**: Java 21
- **Application server**: WebSphere Liberty (Open Liberty), deploy come WAR
- **Framework**: Jakarta EE 10 puro — nessun framework applicativo aggiuntivo (no Spring)
  - JAX-RS (`jakarta.ws.rs`) per le API REST
  - CDI (`jakarta.enterprise`) per dependency injection
  - JPA (`jakarta.persistence`) per la persistenza, tramite Hibernate come provider
  - Bean Validation (`jakarta.validation`) per la validazione degli input
  - JSON-B (`jakarta.json.bind`) per serializzazione JSON
- **Database**: PostgreSQL
- **Build**: Maven (wrapper incluso, `./mvnw`)
- **Autenticazione**: JWT stateless (login → JWT, ruoli come claim: es. admin, socio, volontario)

## Architettura

- Solo API REST, nessun rendering server-side. Il frontend (SPA/mobile) è un consumer separato.
- Package base: `it.def.prolocobe`

## Repository

- Repo Git locale inizializzato, branch principale `main`.
- Destinazione: GitHub (da collegare — `gh` CLI non presente sulla macchina di sviluppo).

## Prossimi passi

1. Definire lo schema dati per soci/tesserati ed eventi (entità JPA).
2. Configurare `persistence.xml` con datasource PostgreSQL (o JNDI datasource lato Liberty).
3. Scaffolding endpoint CRUD minimi per validare il giro completo su Liberty.
4. Introdurre autenticazione JWT (filtro JAX-RS + gestione ruoli).
5. Collegare il repository a GitHub e impostare CI di base (build + test).
