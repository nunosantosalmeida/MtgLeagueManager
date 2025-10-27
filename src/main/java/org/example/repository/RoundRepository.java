package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.WebApplicationException;
import org.example.model.Round;

import java.util.List;

@ApplicationScoped
public class RoundRepository implements PanacheRepository<Round>  {
    @Inject
    EntityManager entityManager;

    public List<Round> listRounds() {
        return entityManager.createNamedQuery("Rounds.findAll", Round.class)
                .getResultList();
    }

    public Round findRound(Integer roundId) {
        Round round = entityManager.find(Round.class, roundId);
        if (round == null) {
            throw new WebApplicationException("Round with id of " + roundId + " does not exist.", 404);
        }
        return round;
    }

    public void persistRound(Round round) {
        entityManager.persist(round);
    }

    public void persistRounds(List<Round> rounds) {
        rounds.forEach(round -> entityManager.persist(round));
    }

}
