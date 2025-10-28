package org.example.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "known_rounds")
@NamedQuery(name = "Rounds.findAll", query = "SELECT r FROM Round r ORDER BY r.date", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
public class Round extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "roundsSequence")
    @SequenceGenerator(name = "roundsSequence", sequenceName = "known_rounds_id_seq", allocationSize = 1, initialValue = 10)
    @Column(name = "round_id", unique = true)
    private Long roundId;

    @Column(name = "round_date")
    private LocalDateTime date;

    @OneToMany(mappedBy = "game_round", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Game> games;

    @Builder.Default
    @Column(name = "is_round_finished")
    private Boolean isRoundFinished = false;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "round_players_not_present",
            joinColumns = @JoinColumn(name = "round_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    @Builder.Default
    private List<Player> playersNotPresent = List.of();

    @Override
    public String toString() {
        StringBuilder roundGamesText = new StringBuilder();
        for (Game roundGame : games) {
            roundGamesText.append(roundGame.toString()).append("<br/>");
        }
        return roundGamesText.toString();
    }
}