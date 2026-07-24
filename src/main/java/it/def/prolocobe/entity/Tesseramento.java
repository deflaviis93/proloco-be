package it.def.prolocobe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tesseramento", uniqueConstraints = @UniqueConstraint(columnNames = {"socio_id", "anno"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Tesseramento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @Column(nullable = false)
    private int anno;

    @Column(nullable = false)
    private BigDecimal importo;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

}
