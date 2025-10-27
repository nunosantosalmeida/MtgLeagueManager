package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.WebApplicationException;
import static org.example.configs.Configs.DRAW_PERCENT_UPGRADE;
import org.example.model.Game;
import org.example.model.Player;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class GameRepository implements PanacheRepository<Game>  {


    @Inject
    EntityManager entityManager;

    @Inject
    PlayerRepository playerRepository;

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

    public void applyGamePointsAndGamesPlayed(Game game) {

        // DRAW
        if (game.isDraw()) {
            List<Player> gamePlayers = game.getGamePlayers();
            float drawPointsSummed = 0F;

            for (Player gamePlayer : gamePlayers) {
                drawPointsSummed = drawPointsSummed + (gamePlayer.getPoints() * DRAW_PERCENT_UPGRADE);
            }

            final float finalDrawPointsSummed = drawPointsSummed;
            gamePlayers.forEach(player -> player.setPoints(player.getPoints() * DRAW_PERCENT_UPGRADE - (finalDrawPointsSummed /gamePlayers.size())));


            gamePlayers.forEach(Player::incrementGamesPlayed);
            gamePlayers.forEach(Player::incrementGamesDrawn);
            playerRepository.persistPlayers(gamePlayers);
        }
        else {
            // WIN
            Player winner = playerRepository.findPlayer(Integer.parseInt(game.getResult()));
            List<Player> gamePlayers = game.getGamePlayers();
            AtomicReference<Float> winnerPointsWon = new AtomicReference<>(0F);
            float minPoints = 500000F; // Arbitrarily large float, just to compare minimum

            for (Player gamePlayer : gamePlayers) {
                if (gamePlayer.getPoints() < minPoints)
                    minPoints = gamePlayer.getPoints();
            }

            gamePlayers.forEach(player -> {
                // This is done in every game, no matter how many players are playing it
                if(!Objects.equals(player.getPlayerId(), winner.getPlayerId())) {
                    float loserPointsLoss = player.getPoints() * 0.07F;
                    player.setPoints(player.getPoints() - loserPointsLoss);
                    player.incrementGamesPlayed();
                    player.incrementGamesLost();
                    winnerPointsWon.updateAndGet(v -> v + loserPointsLoss);
                }
            });

            if (game.getGamePlayers().size() == 5) {
                winner.setPoints(winner.getPoints() + (winnerPointsWon.get()*0.8F));
            } else if (game.getGamePlayers().size() == 3) {
                winner.setPoints(winner.getPoints() + winnerPointsWon.get() + minPoints*0.07F);
            }
            else  {
                winner.setPoints(winner.getPoints() + winnerPointsWon.get());
            }

            winner.incrementGamesPlayed();
            winner.incrementGamesWon();
            playerRepository.persistPlayers(gamePlayers);
        }


    }

    public void deleteGame(Game game) {
        entityManager.remove(game);
    }
}
