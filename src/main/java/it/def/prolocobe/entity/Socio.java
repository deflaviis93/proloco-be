package it.def.prolocobe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "socio")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    @Column(name = "data_nascita")
    private LocalDate dataNascita;

    @Column
    private String telefono;

    @Column
    private String email;

    @Column(name = "data_iscrizione", nullable = false)
    private LocalDate dataIscrizione;

    @OneToOne
    @JoinColumn(name = "utente_id", unique = true)
    private Utente utente;

}
