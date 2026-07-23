package it.def.prolocobe.entity;

import it.def.prolocobe.enums.RuoloUtente;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utente")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "utente_ruolo", joinColumns = @JoinColumn(name = "utente_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "ruolo", nullable = false)
    private Set<RuoloUtente> ruoli = new HashSet<>();

    @Column(nullable = false)
    private boolean attivo = true;

    @Column(name = "deve_cambiare_password", nullable = false)
    private boolean deveCambiarePassword = true;

}
