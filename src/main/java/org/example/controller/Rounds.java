package org.example.controller;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.Round;
import org.example.repository.GameRepository;
import org.example.repository.PlayerRepository;
import org.example.repository.RoundRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
@Path("rounds")
@Slf4j
public class Rounds extends Controller {

    @Inject
    EntityManager em;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private PlayerRepository playerRepository;

    @Inject
    private RoundRepository roundRepository;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance rounds(List<Round> rounds);
        public static native TemplateInstance singleround(Round round);
        public static native TemplateInstance singleUnfinishedRound(Round round);
    }

    @GET
    public TemplateInstance rounds() {
        List<Round> listRounds = roundRepository.listRounds();
        listRounds.sort(Comparator.comparing(Round::getRound_id));
        return Templates.rounds(listRounds);
    }

    @GET
    @Path("{roundId}")
    public TemplateInstance singleround(Integer roundId) {
        Round round = roundRepository.findRound(roundId);
        if (round.getIsRoundFinished())
            return Templates.singleround(round);

        return Templates.singleUnfinishedRound(round);
    }

    @GET
    @Path("delete/{roundId}")
    @Transactional
    public void delete(Integer roundId) {
        Round round = roundRepository.findRound(roundId);
        if (round.getIsRoundFinished()) {
            throw new WebApplicationException("Round with id " + roundId + " was already finished and cannot be deleted.", 404);
        }
        round.getRound_games().forEach(g -> gameRepository.delete(g));
        round.delete();
        rounds();
    }

    @GET
    @Path("newround")
    @Transactional
    public void newround() throws InterruptedException {
        // Listar rondas e verificar se já existe alguma por terminar. Caso nao exista, podemos avançar
        List<Round> listRounds = roundRepository.listRounds();
        if (listRounds.stream().anyMatch(r -> !r.getIsRoundFinished())) {
            rounds();
        }
        // Listar todos os jogadores que não estão presentes
        List<Player> playerListNotPresent = playerRepository.listAll().stream().filter(p -> !p.getIsPresent()).toList();
        Round round = Round.builder()
                .round_date(LocalDateTime.now())
                .players_not_present(playerListNotPresent)
                .round_games(getNewRoundGamesList())
                .build();
        roundRepository.persistRound(round);

        roundRepository.persistRounds(roundRepository.listRounds());

        round.getRound_games().forEach(game -> game.setGame_round(round));
        gameRepository.persistGames(gameRepository.listGames());

        playerRepository.persistPlayers(playerRepository.listPlayers());

        listRounds.sort(Comparator.comparing(Round::getRound_id));
        rounds();
    }

    @GET
    @Path("closeround/{roundId}")
    @Transactional
    public void closeround(Integer roundId) {
        Round round = roundRepository.findRound(roundId);
        if (round.getRound_games().stream().anyMatch(g -> !g.isFinished())) {
            rounds();
            return;
        }
        // Aplicar penalidade aos jogadores que NÃO participaram na ronda
        playerRepository.applyPenalty(round.getPlayers_not_present());

        round.getRound_games().forEach(rg -> gameRepository.applyGamePointsAndGamesPlayed(rg));

        round.setIsRoundFinished(true);

        // TODO
        roundRepository.persistRound(round);
        round.getRound_games().forEach(game -> game.setGame_round(round));
        gameRepository.persistGames(gameRepository.listGames());
        playerRepository.persistPlayers(playerRepository.listPlayers());
        roundRepository.persistRounds(roundRepository.listRounds());
        // TODO

        rounds();
    }

    // Round Distribution Logic
    private List<Game> getNewRoundGamesList() {

        List<Player> playerListPresent = new ArrayList<>(playerRepository.listAll().stream().filter(Player::getIsPresent).toList());

        // Encontrar a melhor distribuição de mesas possível para o numero de jogadores presentes
        List<Integer> podDistribution = getPodDistribution(playerListPresent.size());

        // Distribuir of jogadores aleatoriamente com a distribuição encontrada
        Collections.shuffle(playerListPresent);
        List<List<Player>> gamePods = splitPlayers(playerListPresent, podDistribution);

        List<Game> listGames = new ArrayList<>();
        for (int i = 0; i < gamePods.size(); i++) {
            listGames.add(
                    Game.builder()
                            .gameDate(LocalDateTime.now())
                            .gamePlayers(gamePods.get(i))
                            .build()
            );
        }

        return listGames;
    }

    private List<List<Player>> splitPlayers(List<Player> playerListPresent, List<Integer> listRoundTables) {

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

    private List<Integer> getPodDistribution(int size) {

        // Se o  numero de jogadores for inferior ou igual a 5, devolver apenas uma mesa com ess numero de jogadores
        if (size >= 0 && size <= 5)
            return List.of(size);

        // Caso contrário, distribuir jogadores por mesas de 3 e 4 jogadores.
        int playersToDistribute = size;
        List<Integer> listRoundTables = new ArrayList<>(List.of());

        // Juntar o mínimo numero de mesas de 3 possível
        while (playersToDistribute % 4 > 0) {
            listRoundTables.add(3);
            playersToDistribute -= 3;
        }

        // Juntar em mesas de 4 os jogadores restantes
        for (int i = 0; i < playersToDistribute / 4; i++)
            listRoundTables.add(4);

        return listRoundTables;  // exemplo: size = 11, listRoundTables[3,4,4]
    }

}