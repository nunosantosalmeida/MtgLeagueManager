package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import static org.example.Configs.PENALTY_MULTIPLIER;
import org.example.model.Player;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@ApplicationScoped
public class PlayerRepository implements PanacheRepository<Player> {

    @Inject
    EntityManager entityManager;

    public List<Player> listPlayers() {
        List<Player> listPlayers = entityManager.createNamedQuery("Players.findAll", Player.class).getResultList();
        sortByRanking(listPlayers);
        return listPlayers;
    }

    public Player findPlayer(final Integer id) {
        return entityManager.find(Player.class, id);
    }

    public void addNewPlayer(final String name, final String email, final String decklist, final float points) {
        Player playerInsert = Player.builder()
                .name(name)
                .email(email)
                .decklist(decklist)
                .points(points)
                .isPresent(true)
                .build();
        this.persistPlayer(playerInsert);
    }

    public void deletePlayer(final Player player) {
        player.delete();
    }

    public void persistPlayer(final Player player) {
        entityManager.persist(player);
    }

    public void persistPlayers(final List<Player> players) {
        players.forEach(player -> entityManager.persist(player));
    }

    public void applyPenalty(final List<Player> playerListNotPresent) {
        playerListNotPresent.forEach(p -> p.setPoints(p.getPoints() * PENALTY_MULTIPLIER));
        persistPlayers(playerListNotPresent);
    }

    public void toggleIsPresent(Player player) {
        player.setPresent(!player.isPresent());
        persistPlayer(player);
    }

    public void sortByRanking(List<Player> listPlayers) {
        listPlayers.sort(Comparator.comparing(Player::getPoints, Comparator.reverseOrder())
                .thenComparing(Player::getGamesPlayed, Comparator.reverseOrder())
                .thenComparing(Player::getGamesWon, Comparator.reverseOrder()));

        // Rank players
        for (int i = 0; i < listPlayers.size(); i++) {
            listPlayers.get(i).setRank(i + 1);
        }
    }
}
