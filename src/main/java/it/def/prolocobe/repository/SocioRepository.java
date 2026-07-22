package it.def.prolocobe.repository;

import it.def.prolocobe.entity.Socio;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SocioRepository {

    @PersistenceContext(unitName = "oracleDB")
    private EntityManager em;

    @Transactional
    public Socio save(Socio socio) {
        em.persist(socio);
        return socio;
    }

    public List<Socio> findAll() {
        return em.createQuery("SELECT s FROM Socio s", Socio.class).getResultList();
    }
}
