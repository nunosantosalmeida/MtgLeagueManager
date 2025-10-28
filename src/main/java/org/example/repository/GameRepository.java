package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.WebApplicationException;
import org.example.model.Game;

import java.util.List;

@ApplicationScoped
public class GameRepository implements PanacheRepository<Game>  {


    @Inject
    EntityManager entityManager;

    public List<Game> listGames() {
        return entityManager.createNamedQuery("Games.findAll", Game.class)
                .getResultList();
    }

    public Game findGame(Integer gameId) {
        Game game = entityManager.find(Game.class, gameId);;
        if (game == null) {
            throw new WebApplicationException("Game with id of " + gameId + " does not exist.", 404);
        }
        return game;
    }

    public void persistGame(Game game) {
        entityManager.persist(game);
    }

    public void persistGames(List<Game> games) {
        games.forEach(game -> entityManager.persist(game));
    }

    public void deleteGame(Game game) {
        entityManager.remove(game);
    }
}
