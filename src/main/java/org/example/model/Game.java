package org.example.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Getter
@Setter
@Entity
@Table(name = "known_games")
@NamedQuery(name = "Games.findAll", query = "SELECT g FROM Game g ORDER BY g.date", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Accessors(chain = true)
public class Game extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "gamesSequence")
    @SequenceGenerator(name = "gamesSequence", sequenceName = "known_games_id_seq", allocationSize = 1, initialValue = 10)
    @Column(name = "game_id", unique = true)
    private Long gameId;

    @Builder.Default
    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    @Builder.Default
    private String result = "";

    @Builder.Default
    @Column(name = "is_finished")
    private boolean finished = false;

    @Builder.Default
    @Column(name = "is_draw")
    private boolean isDraw = false;

    @ManyToOne
    @JoinColumn(name = "round_games")
    private Round game_round;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Getter
    private List<Player> players;

    @Override
    public String toString() {
        StringBuilder game_players_text = new StringBuilder("[ ");
        for (Player player : players) {
            game_players_text.append(player.toString());
            if (players.indexOf(player) < players.size() - 1){
                game_players_text.append(" | ");
            }
        }
        return game_players_text.append(" ]").toString();
    }
}