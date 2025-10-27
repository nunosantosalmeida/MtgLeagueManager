package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.example.model.Player;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@ApplicationScoped
public class PlayerRepository implements PanacheRepository<Player> {

    @Inject
    EntityManager entityManager;

    public List<Player> listPlayers() {
        return entityManager.createNamedQuery("Players.findAll", Player.class)
                .getResultList();
    }

    public Player findPlayer(Integer id) {
        return entityManager.find(Player.class, id);
    }

    public void addNewPlayer(String name, String email, String decklist, float points) {
        Player playerInsert = Player.builder()
                .name(name)
                .email(email)
                .decklist(decklist)
                .points(points)
                .dateRegistered(LocalDateTime.now())
                .isPresent(true)
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .gamesDrawn(0)
                .build();
        this.persistPlayer(playerInsert);
    }

    public void deletePlayer(Player player) {
        player.delete();
    }

    public void persistPlayer(Player player) {
        entityManager.persist(player);
    }

    public void persistPlayers(List<Player> players) {
        players.forEach(player -> entityManager.persist(player));
    }

    public void applyPenalty(List<Player> playerListNotPresent) {
        playerListNotPresent.forEach(p -> p.setPoints(p.getPoints() * 0.93f));
        playerListNotPresent.forEach(this::persistPlayer);
    }

    public void toggleIsPresent(Player player) {
        player.setIsPresent(!player.getIsPresent());
        persistPlayer(player);
    }

    public void sortByRanking(List<Player> listPlayers) {
        listPlayers.sort(Comparator.comparing(Player::getPoints, Comparator.reverseOrder())
                .thenComparing(Player::getGamesPlayed, Comparator.reverseOrder())
                .thenComparing(Player::getGamesWon, Comparator.reverseOrder()));

        // Rank players
        for(int i = 0; i < listPlayers.size(); i++) {
            listPlayers.get(i).setRank(i+1);
        }
    }
}
