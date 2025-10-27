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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;


@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "date")
    private LocalDateTime date;

    @Builder.Default
    private String result = "";

    @Builder.Default
    @Column(name = "is_finished")
    private boolean finished = false;

    @Builder.Default
    @Column(name = "is_draw")
    private boolean isDraw = false;

    //@ManyToOne(cascade = CascadeType.PERSIST)
    @ManyToOne
    @JoinColumn(name = "round_games")
    private Round game_round;

    @ManyToMany(cascade = CascadeType.MERGE,
            fetch=FetchType.EAGER)
    @Getter
    private List<Player> gamePlayers;

    @Override
    public String toString() {
        String game_players_text = "[";
        for(int i = 0; i < gamePlayers.size(); i++) {
            game_players_text = game_players_text.concat(gamePlayers.get(i).toString());
            if(i!=gamePlayers.size()-1)
                game_players_text = game_players_text.concat( ", ");
        }

        return game_players_text + "]";
    }
}