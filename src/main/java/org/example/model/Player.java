package org.example.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import static org.example.Configs.STARTING_POINTS;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Getter
@Setter
@Entity
@Table(name = "known_players")
@NamedQuery(name = "Players.findAll", query = "SELECT p FROM Player p ORDER BY p.name", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
public class Player extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "playersSequence")
    @SequenceGenerator(name = "playersSequence", sequenceName = "known_players_id_seq", allocationSize = 1, initialValue = 10)
    @Column(name = "player_id", unique = true)
    private Integer playerId;

    @Builder.Default
    @Column(name = "dateRegistered")
    private LocalDateTime dateRegistered = LocalDateTime.now();

    @Builder.Default
    @Column(name = "rank", unique = true)
    private Integer rank = 0; // TODO on next iteration of testing this should be a primitive type

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "decklist")
    private String decklist;

    @Builder.Default
    @Column(name = "points")
    private float points = STARTING_POINTS;

    @Builder.Default
    @NotNull
    @Column(name = "is_present")
    private boolean isPresent = false;

    @Builder.Default
    @Column(name = "gamesPlayed")
    private int gamesPlayed = 0;

    @Builder.Default
    @Column(name = "gamesWon")
    private int gamesWon = 0;

    @Builder.Default
    @Column(name = "gamesLost")
    private int gamesLost = 0;

    @Builder.Default
    @Column(name = "gamesDrawn")
    private int gamesDrawn = 0;

    @ManyToMany(mappedBy = "playersNotPresent", fetch = FetchType.EAGER)
    @Builder.Default
    private List<Round> not_present = List.of();


    @Override
    public String toString() {
        return this.getName();
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed += 1;
    }

    public void incrementGamesWon() {
        this.gamesWon += 1;
    }

    public void incrementGamesLost() {
        this.gamesLost += 1;
    }

    public void incrementGamesDrawn() {
        this.gamesDrawn += 1;
    }
}
