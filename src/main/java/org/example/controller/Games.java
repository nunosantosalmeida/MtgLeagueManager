package org.example.controller;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.example.mapper.GameMapper;
import org.example.model.Game;
import org.example.model.Player;
import org.example.repository.GameRepository;
import org.example.repository.PlayerRepository;
import org.example.view.GameView;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
@Path("games")
public class Games {

    @Inject
    GameMapper gameMapper;

    @Inject
    GameRepository gameRepository;

    @Inject
    PlayerRepository playerRepository;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance games(final List<GameView> gameViews);
        public static native TemplateInstance singleGame(final GameView gameView);
        public static native TemplateInstance newGame(final List<Player> players);
    }

    @GET
    public TemplateInstance games() {
        List<Game> listGames = gameRepository.listGames();
        listGames.sort(Comparator.comparing(Game::getDate).reversed());
        return Templates.games(gameMapper.toListGameViews(listGames));
    }

    @GET
    @Path("{id}")
    public TemplateInstance singleGame(final Integer id) {
        Game game = gameRepository.findGame(id);
        return Templates.singleGame(gameMapper.toGameView(game));
    }

    @GET
    @Path("newgame")
    public TemplateInstance newGame() {
        List<Player> players = playerRepository.listPlayers();
        return Templates.newGame(players);
    }

    @POST
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("addgame")
    public TemplateInstance addGame(@RestQuery final String confirmationCode,
                                    @RestForm final String date,
                                    @RestForm final List<Integer> listPlayersIds,
                                    @RestForm final String result) {

        List<Player> gamePlayers = new ArrayList<>();

        for (Integer id : listPlayersIds) {
            Player player = playerRepository.findPlayer(id);
            gamePlayers.add(player);
        }

        Collections.shuffle(gamePlayers);

        Game gameInsert = Game.builder()
            .date(date == null || date.isBlank() ? LocalDateTime.now() : LocalDateTime.parse(date))
            .players(gamePlayers)
            .result(result == null || result.isBlank() ? "" : result)
            .build();
        gameRepository.persistGame(gameInsert);
        return games();
    }

    @GET
    @Path("delete/{gameId}")
    @Transactional
    public void delete(final Integer gameId) {
        Game game = gameRepository.findGame(gameId);
        game.delete();
        games();
    }

    @GET
    @Transactional
    @Path("setwinner")
    public TemplateInstance setWinner(@RestQuery final String gameId,
                                      @RestQuery final Integer playerId) {
        Game game = gameRepository.findGame(Integer.parseInt(gameId));

        if (playerId.equals(0)) {
            game.setDraw(true);
            game.setResult("Draw");
        } else {
            Player player = playerRepository.findPlayer(playerId);
            if (player == null) {
                throw new WebApplicationException("Player with id " + playerId + " does not exist.", 404);
            }
            game.setResult(playerId.toString());
        }

        game.setFinished(true);
        gameRepository.persist(game);
        List<Game> listGames = gameRepository.listGames();
        listGames.sort(Comparator.comparing(Game::getDate));
        Game updatedGame = gameRepository.findGame(Integer.parseInt(gameId));

        return Templates.singleGame(gameMapper.toGameView(updatedGame));
    }

}
