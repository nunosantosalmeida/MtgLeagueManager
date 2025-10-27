package org.example.controller;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
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
    EntityManager em;

    @Inject
    GameMapper gameMapper;

    @Inject
    GameRepository gameRepository;

    @Inject
    PlayerRepository playerRepository;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance games(List<GameView> gameviews);

        public static native TemplateInstance singlegame(GameView gameview);

        public static native TemplateInstance newgame(List<Player> players);
    }

    @GET
    public TemplateInstance games() {
        List<Game> listGames = gameRepository.listGames();
        listGames.sort(Comparator.comparing(Game::getGameDate));
        return Templates.games(listGamesToListGameViews(listGames));
    }

    @GET
    @Path("{id}")
    public TemplateInstance singleGame(Integer id) {
        Game game = gameRepository.findGame(id);
        return Templates.singlegame(gameMapper.toGameView(game));
    }

    @GET
    @Path("newgame")
    public TemplateInstance newgame() {
        List<Player> players = playerRepository.listPlayers();
        return Templates.newgame(players);
    }

    @POST
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("addgame")
    public TemplateInstance addgame(@RestQuery String confirmationCode,
        @RestForm String date,
        @RestForm List<Integer> listPlayersIds,
        @RestForm String result) {

        List<Player> gamePlayers = new ArrayList<>();

        for (Integer id : listPlayersIds) {
            Player player = playerRepository.findPlayer(id);
            gamePlayers.add(player);
        }

        Collections.shuffle(gamePlayers);

        Game gameInsert = Game.builder()
            .gameDate(date == null || date.isBlank() ? LocalDateTime.now() : LocalDateTime.parse(date))
            .gamePlayers(gamePlayers)
            .result(result == null || result.isBlank() ? "" : result)
            .build();
        gameRepository.persistGame(gameInsert);
        return games();
    }

    @GET
    @Path("delete/{gameId}")
    @Transactional
    public void delete(Integer gameId) {
        Game game = gameRepository.findGame(gameId);
        game.delete();
        games();
    }

    @GET
    @Transactional
    @Path("setwinner")
    public TemplateInstance setwinner(@RestQuery String game_id, @RestQuery Integer player_id) {
        Game game = gameRepository.findGame(Integer.parseInt(game_id));

        if (player_id.equals(0)) {
            game.setDraw(true);
            game.setResult("Draw");
        } else {
            Player player = playerRepository.findPlayer(player_id);
            if (player == null) {
                throw new WebApplicationException("Player with id " + player_id + " does not exist.", 404);
            }
            game.setResult(player_id.toString());
        }

        game.setFinished(true);
        gameRepository.persist(game);
        List<Game> listGames = gameRepository.listGames();
        listGames.sort(Comparator.comparing(Game::getGameDate));
        Game updatedGame = gameRepository.findGame(Integer.parseInt(game_id));

        return Templates.singlegame(gameMapper.toGameView(updatedGame));
    }

    private List<GameView> listGamesToListGameViews(List<Game> listGames) {
        List<GameView> listGameViews = new ArrayList<>(List.of());
        listGames.forEach(game -> listGameViews.add(gameMapper.toGameView(game)));
        return listGameViews;
    }

}
