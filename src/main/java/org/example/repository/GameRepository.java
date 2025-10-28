package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.WebApplicationException;
import org.example.model.Game;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class GameRepository implements PanacheRepository<Game> {

    @Inject
    EntityManager entityManager;

    public List<Game> listGames() {
        List<Game> listGames = entityManager.createNamedQuery("Games.findAll", Game.class).getResultList();
        listGames.sort(Comparator.comparing(Game::getDate).reversed());
        return listGames;
    }

    public Game findGame(final Integer gameId) {
        Game game = entityManager.find(Game.class, gameId);
        if (game == null) {
            throw new WebApplicationException("Game with id of " + gameId + " does not exist.", 404);
        }
        return game;
    }

    public void deleteGame(final Game game) {
        game.delete();
    }

    public void persistGame(final Game game) {
        entityManager.persist(game);
    }

    public void persistGames(final List<Game> games) {
        games.forEach(game -> entityManager.persist(game));
    }
}
