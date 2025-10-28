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
import lombok.extern.slf4j.Slf4j;
import static org.example.Configs.DATE_FORMAT;
import static org.example.Configs.DRAW_POINTS_DISTRIBUTION_MULTIPLIER;
import static org.example.Configs.FIVE_PLAYER_POD_WIN_PONDERATION;
import static org.example.Configs.LOSS_MULTIPLIER;
import static org.example.Configs.THREE_PLAYER_POD_PONDERATION;
import static org.example.Configs.TIME_FORMAT;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.Round;
import org.example.repository.GameRepository;
import org.example.repository.PlayerRepository;
import org.example.repository.RoundRepository;
import org.example.view.RoundView;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
@Path("rounds")
@Slf4j
public class Rounds extends Controller {

    @Inject
    private GameRepository gameRepository;

    @Inject
    private PlayerRepository playerRepository;

    @Inject
    private RoundRepository roundRepository;

    public Rounds(final GameRepository gameRepository, final PlayerRepository playerRepository, final RoundRepository roundRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.roundRepository = roundRepository;
    }

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance rounds(final List<RoundView> rounds);
        public static native TemplateInstance newRound(final List<Player> playersNotPresent,
                                                       final List<Player> playersPresent,
                                                       final Map<String, String> flash);
        public static native TemplateInstance singleRoundFinished(final RoundView round);
        public static native TemplateInstance singleRoundNotFinished(final RoundView round);
    }

    @GET
    public TemplateInstance rounds() {
        List<Round> listRounds = roundRepository.listRounds();
        listRounds.sort(Comparator.comparing(Round::getRoundId));
        List<RoundView> listRoundsView = listRounds.stream().map(this::roundToRoundView).toList();
        return Templates.rounds(listRoundsView);
    }

    @GET
    @Path("{roundId}")
    public TemplateInstance singleRound(final Integer roundId) {
        Round round = roundRepository.findRound(roundId);
        if (round.getIsRoundFinished())
            return Templates.singleRoundFinished(roundToRoundView(round));

        return Templates.singleRoundNotFinished(roundToRoundView(round));
    }

    @GET
    @Transactional
    @Path("delete/{roundId}")
    public void delete(final Integer roundId) {
        Round round = roundRepository.findRound(roundId);
        if (round.getIsRoundFinished()) {
            throw new WebApplicationException("Round with id " + roundId + " was already finished and cannot be deleted.", 404);
        }
        round.getGames().forEach(g -> gameRepository.delete(g));
        round.delete();
        rounds();
    }

    @GET
    @Path("newround")
    public TemplateInstance newround() {
        Map<String, String> flash = new HashMap<>();
        flash.put("error", ""); // Set the error message
        return Templates.newRound(playerRepository.listAll().stream().filter(p -> !p.isPresent()).toList(),
                playerRepository.listAll().stream().filter(Player::isPresent).toList(),
                flash);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Path("newroundOLD")
    public void newroundOLD(@RestQuery final String confirmationCode) {
        // Listar rondas e verificar se já existe alguma por terminar. Caso nao exista, podemos avançar
        List<Round> listRounds = roundRepository.listRounds();
        if (listRounds.stream().anyMatch(r -> !r.getIsRoundFinished())) {
            rounds();
        }
        // Listar todos os jogadores que não estão presentes
        List<Player> playerListNotPresent = playerRepository.listAll().stream().filter(p -> !p.isPresent()).toList();
        Round round = Round.builder()
                .date(LocalDateTime.now())
                .playersNotPresent(playerListNotPresent)
                .games(getNewRoundGamesList(playerRepository.listAll().stream().filter(Player::isPresent).toList()))
                .build();
        roundRepository.persistRound(round);

        roundRepository.persistRounds(roundRepository.listRounds());

        round.getGames().forEach(game -> game.setGame_round(round));
        gameRepository.persistGames(gameRepository.listGames());

        playerRepository.persistPlayers(playerRepository.listPlayers());

        listRounds.sort(Comparator.comparing(Round::getRoundId));
        rounds();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Path("newround")
    public TemplateInstance newroundNew(@RestQuery String confirmationCode, @RestForm List<Integer> listPlayersIds) {

        Map<String, String> flash = new HashMap<>();
        final List<Player> playersInVenue = new ArrayList<>();

        for (Integer id : listPlayersIds) {
            Player player = playerRepository.findPlayer(id);
            playersInVenue.add(player);
        }

        if (playersInVenue.size() < 3) {
            flash.put("error", "Cant start a round with less than 3 players!"); // Set the error message
            return Templates.newRound(playerRepository.listAll().stream().filter(p -> !p.isPresent()).toList(),
                    playerRepository.listAll().stream().filter(Player::isPresent).toList(),
                    flash);
        }

        Collections.shuffle(playersInVenue);

        // Listar rondas e verificar se já existe alguma por terminar. Caso nao exista, podemos avançar
        List<Round> listRounds = roundRepository.listRounds();
        if (listRounds.stream().anyMatch(r -> !r.getIsRoundFinished())) {
            rounds();
        }
        // Listar todos os jogadores que não estão presentes
        List<Player> playerListNotPresent = playerRepository.listAll();
        playerListNotPresent.removeIf(playersInVenue::contains);

        Round round = Round.builder()
                .date(LocalDateTime.now())
                .playersNotPresent(playerListNotPresent)
                .games(getNewRoundGamesList(playersInVenue))
                .build();

        roundRepository.persistRound(round);
        roundRepository.persistRounds(roundRepository.listRounds());
        round.getGames().forEach(game -> game.setGame_round(round));
        gameRepository.persistGames(gameRepository.listGames());

        playerRepository.persistPlayers(playerRepository.listPlayers());

        listRounds.sort(Comparator.comparing(Round::getRoundId));
        return rounds();
    }

    @GET
    @Transactional
    @Path("closeround/{roundId}")
    public void closeround(final Integer roundId) {
        Round round = roundRepository.findRound(roundId);
        if (round.getGames().stream().anyMatch(g -> !g.isFinished())) {
            rounds();
            return;
        }
        // Aplicar penalidade aos jogadores que NÃO participaram na ronda
        playerRepository.applyPenalty(round.getPlayersNotPresent());

        round.getGames().forEach(this::applyGamePointsAndGamesPlayed);

        round.setIsRoundFinished(true);

        // TODO
        roundRepository.persistRound(round);
        round.getGames().forEach(game -> game.setGame_round(round));
        gameRepository.persistGames(gameRepository.listGames());
        playerRepository.persistPlayers(playerRepository.listPlayers());
        roundRepository.persistRounds(roundRepository.listRounds());
        // TODO

        rounds();
    }

    private List<Game> getNewRoundGamesList(final List<Player> playerListPresent) {

        // Get the optimal pod distribution for the available players
        List<Integer> podDistribution = getRoundPodsConfiguration(playerListPresent.size());

        // Distribute players into random pods using the optimal distribution
        Collections.shuffle(playerListPresent);
        List<List<Player>> roundPods = splitPlayers(playerListPresent, podDistribution);

        List<Game> listGames = new ArrayList<>();
        for (List<Player> roundPod : roundPods) {
            listGames.add(
                    Game.builder()
                            .date(LocalDateTime.now())
                            .players(roundPod)
                            .build()
            );
        }

        return listGames;
    }

    private List<List<Player>> splitPlayers(final List<Player> playerListPresent, final List<Integer> listRoundTables) {

        List<List<Player>> result = new ArrayList<>();

        int index = 0;
        for (int size : listRoundTables) {
            // Ensure we don't go out of bounds
            if (index + size <= playerListPresent.size()) {
                List<Player> sublist = new ArrayList<>(playerListPresent.subList(index, index + size));
                result.add(sublist);
                index += size; // Move the index forward
            } else {
                // Handle cases where sizes exceed original list size
                throw new IllegalArgumentException("Size exceeds original list size.");
            }
        }

        return result;
    }

    private List<Integer> getRoundPodsConfiguration(final int roundPlayers) {

        // If the total players is less than 5, return a single pod with that number of players
        if (roundPlayers >= 0 && roundPlayers <= 5)
            return List.of(roundPlayers);

        // Otherwise, distribute players into 3 and 4 player pods
        int playersToDistribute = roundPlayers;
        List<Integer> roundPodsDistribution = new ArrayList<>(List.of());

        // Distribute the least amount of 3 player pods possible
        while (playersToDistribute % 4 > 0) {
            roundPodsDistribution.add(3);
            playersToDistribute -= 3;
        }

        // Distribute the remaining players into 4 player pods
        for (int i = 0; i < playersToDistribute / 4; i++)
            roundPodsDistribution.add(4);

        return roundPodsDistribution;  // example: size = 11, listRoundTables[3,4,4]
    }

    private void applyGamePointsAndGamesPlayed(final Game game) {
        // Game ended in a DRAW
        if (game.isDraw()) {
            processGameAsDraw(game);
        }
        else {
            // Game ended in a WIN
            processGameAsWin(game);
        }
    }

    private void processGameAsWin(final Game game) {
        Player winner = playerRepository.findPlayer(Integer.parseInt(game.getResult()));
        List<Player> gamePlayers = game.getPlayers();
        AtomicReference<Float> winnerPointsWon = new AtomicReference<>(0F);
        float minPoints = 500000F; // Arbitrarily large float, just to compare minimum

        for (Player gamePlayer : gamePlayers) {
            if (gamePlayer.getPoints() < minPoints)
                minPoints = gamePlayer.getPoints();
        }

        gamePlayers.forEach(player -> {
            // This is done in every game, no matter how many players are playing it
            if (!Objects.equals(player.getPlayerId(), winner.getPlayerId())) {
                float loserPointsLoss = player.getPoints() * LOSS_MULTIPLIER;
                player.setPoints(player.getPoints() - loserPointsLoss);
                player.incrementGamesPlayed();
                player.incrementGamesLost();
                winnerPointsWon.updateAndGet(v -> v + loserPointsLoss);
            }
        });

        if (game.getPlayers().size() == 5) {
            // Win in a 5-player-pod: winner will only get 80% of the points it would normally get
            winner.setPoints(winner.getPoints() + (winnerPointsWon.get() * FIVE_PLAYER_POD_WIN_PONDERATION));
        } else if (game.getPlayers().size() == 3) {
            // Win in a 3-player-pod: winner gets additional points equal to 7% of the player with the minimum points
            winner.setPoints(winner.getPoints() + winnerPointsWon.get() + minPoints * THREE_PLAYER_POD_PONDERATION);
        }
        else  {
            winner.setPoints(winner.getPoints() + winnerPointsWon.get());
        }

        winner.incrementGamesPlayed();
        winner.incrementGamesWon();
        playerRepository.persistPlayers(gamePlayers);
    }

    private void processGameAsDraw(final Game game) {
        List<Player> gamePlayers = game.getPlayers();
        float drawPointsSumToDistribute = 0F;

        for (Player gamePlayer : gamePlayers) {
            drawPointsSumToDistribute = drawPointsSumToDistribute + (gamePlayer.getPoints() * DRAW_POINTS_DISTRIBUTION_MULTIPLIER);
        }

        final float finalDrawPointsSummed = drawPointsSumToDistribute;
        gamePlayers.forEach(player -> player.setPoints(player.getPoints() * DRAW_POINTS_DISTRIBUTION_MULTIPLIER - (finalDrawPointsSummed /gamePlayers.size())));

        gamePlayers.forEach(Player::incrementGamesPlayed);
        gamePlayers.forEach(Player::incrementGamesDrawn);
        playerRepository.persistPlayers(gamePlayers);
    }

    private RoundView roundToRoundView(Round round) {
        return RoundView.builder()
                .roundId(round.getRoundId())
                .date(round.getDate().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern(DATE_FORMAT)) + " - " + round.getDate().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern(TIME_FORMAT)))
                .games(round.getGames())
                .isRoundFinished(round.getIsRoundFinished())
                .playersNotPresent(round.getPlayersNotPresent())
                .build();
    }
}