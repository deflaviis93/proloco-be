package it.def.prolocobe.repository;

import it.def.prolocobe.entity.Tesseramento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TesseramentoRepository extends JpaRepository<Tesseramento, Long> {

    List<Tesseramento> findBySocioIdOrderByAnnoDesc(Long socioId);
}
