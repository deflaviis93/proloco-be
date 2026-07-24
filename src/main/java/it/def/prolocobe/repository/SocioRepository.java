package it.def.prolocobe.repository;

import it.def.prolocobe.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocioRepository extends JpaRepository<Socio, Long> {

    Optional<Socio> findByUtenteId(Long utenteId);
}
