# Proloco BE — Nerito

Backend per la gestione della Pro Loco del paese di Nerito.

## Stato del progetto

Progetto nuovo, in fase di avvio. Lo scheletro tecnico esiste (build Maven, Spring Boot,
deploy Docker), il dominio applicativo (entità, endpoint, DB) è ancora da costruire.

Nota: il progetto è partito su Jakarta EE puro + Open Liberty, poi migrato a Spring Boot
per ridurre l'attrito operativo (jar eseguibile invece di application server, deploy più
semplice) — scelta corretta per un progetto community-size senza vincoli enterprise.

## Scope funzionale (fase 1)

- **Gestione socie/tesserati**: anagrafica soci, tessere, quote associative, scadenze di rinnovo.
- **Eventi e manifestazioni**: creazione eventi/sagre, programma, turni volontari, prenotazioni posti.

Fuori scope per ora (da valutare in fasi successive): contabilità/entrate-uscite, gestione
turni volontari granulare per stand/cucina/biglietteria.

## Stack tecnico

- **Linguaggio**: Java 21
- **Framework**: Spring Boot 3 (Spring MVC per le API REST, Spring Data JPA per la persistenza, Spring Validation per la validazione degli input)
- **Database**: PostgreSQL, provider JPA Hibernate (via Spring Data JPA)
- **Build**: Maven (wrapper incluso, `./mvnw`), packaging jar eseguibile (`spring-boot-maven-plugin`)
- **Deploy**: Docker — immagine multi-stage (build Maven → runtime `eclipse-temurin:21-jre`), orchestrata con `docker-compose.yml` insieme al container Postgres
- **Autenticazione**: JWT stateless (login → JWT, ruoli come claim: es. admin, socio, volontario) — da introdurre (Spring Security)

## Architettura

- Solo API REST, nessun rendering server-side. Il frontend (SPA/mobile) è un consumer separato.
- Package base: `it.def.prolocobe`

## Repository

- Repo Git locale inizializzato, branch principale `main`.
- Destinazione: GitHub (da collegare — `gh` CLI non presente sulla macchina di sviluppo).

## Prossimi passi

1. Definire lo schema dati per soci/tesserati ed eventi (entità JPA).
2. Scaffolding endpoint CRUD minimi per validare il giro completo (controller → service → repository → DB).
3. Introdurre autenticazione JWT (Spring Security, filtro + gestione ruoli).
4. Collegare il repository a GitHub e impostare CI di base (build + test).
