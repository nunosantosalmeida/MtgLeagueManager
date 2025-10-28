package org.example.controller;

import io.quarkiverse.renarde.Controller;
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
import static org.example.Configs.STARTING_POINTS;
import org.example.model.Player;
import org.example.repository.PlayerRepository;
import org.example.view.PlayerView;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@ApplicationScoped
@Path("players")
public class Players extends Controller {

    @Inject
    PlayerRepository playerRepository;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance players(final List<PlayerView> players);
        public static native TemplateInstance singlePlayer(final PlayerView player);
        public static native TemplateInstance newPlayer(final int startingPoints);
    }

    @GET
    public TemplateInstance players() {
        List<Player> listPlayers = playerRepository.listPlayers();
        playerRepository.sortByRanking(listPlayers);
        return Templates.players(playerListToPlayerViewList(listPlayers));
    }

    @GET
    @Path("{id}")
    public TemplateInstance singleplayer(final Integer id) {
        Player player = playerRepository.findPlayer(id);
        if (player == null) {
            throw new WebApplicationException("Player with id of " + id + " does not exist.", 404);
        }
        return Templates.singlePlayer(playerToPlayerView(player));
    }

    @GET
    @Path("toggleispresent/{id}")
    @Transactional
    public void toggleIsPresent(final Integer id) {
        Player player = playerRepository.findPlayer(id);
        if (player == null) {
            throw new WebApplicationException("Player with id of " + id + " does not exist.", 404);
        }
        playerRepository.toggleIsPresent(player);
    }

    @GET
    @Path("newplayer")
    public TemplateInstance newplayer() {
        return Templates.newPlayer(STARTING_POINTS);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("addplayer")
    public void addplayer(@RestQuery final String confirmationCode,
                          @RestForm final String name,
                          @RestForm final String email,
                          @RestForm final String decklist,
                          @RestForm final float points) {
        playerRepository.addNewPlayer(name, email, decklist, points);
        players();
    }

    @GET
    @Path("delete/{playerId}")
    @Transactional
    public void delete(final Integer playerId) {
        Player player = playerRepository.findPlayer(playerId);
        if (player == null) {
            throw new WebApplicationException("Player with id of " + playerId + " does not exist.", 404);
        }
        playerRepository.deletePlayer(player);
        players();
    }

    private List<PlayerView> playerListToPlayerViewList(final List<Player> playerList) {
        return playerList.stream()
                .map(this::playerToPlayerView)
                .toList();
    }

    private PlayerView playerToPlayerView(final Player player) {
        return PlayerView.builder()
                .playerId(player.getPlayerId())
                .name(player.getName())
                .email(player.getEmail())
                .decklist(player.getDecklist())
                .dateRegistered(player.getDateRegistered().toString())
                .rank(player.getRank())
                .points(String.format("%.2f", player.getPoints()))
                .isPresent(player.getIsPresent())
                .gamesPlayed(player.getGamesPlayed())
                .gamesWon(player.getGamesWon())
                .gamesLost(player.getGamesLost())
                .gamesDrawn(player.getGamesDrawn())
                .build();
    }
}
