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
@Table(name = "known_players")
@NamedQuery(name = "Players.findAll", query = "SELECT p FROM Player p ORDER BY p.name", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
public class Player extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "playersSequence")
    @SequenceGenerator(name = "playersSequence", sequenceName = "known_players_id_seq", allocationSize = 1, initialValue = 10)
    @Column(name = "player_id")
    private Integer playerId;

    @Column(name = "dateRegistered")
    private LocalDateTime dateRegistered;

    @Builder.Default
    private Integer rank = 0;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "decklist")
    private String decklist;

    @Column(name = "points")
    private float points;

    @Builder.Default
    @NotNull
    @Column(name = "is_present")
    private Boolean isPresent = false;

    @Column(name = "gamesPlayed")
    private Integer gamesPlayed;

    @Column(name = "gamesWon")
    private Integer gamesWon;

    @Column(name = "gamesLost")
    private Integer gamesLost;

    @Column(name = "gamesDrawn")
    private Integer gamesDrawn;

    @ManyToMany(mappedBy = "playersNotPresent",
            fetch=FetchType.EAGER)
    @Builder.Default
    private List<Round> not_present = List.of();


    @Override
    public String toString() {
        return this.getName();
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed+=1;
    }

    public void incrementGamesWon() {
        this.gamesWon+=1;
    }

    public void incrementGamesLost() {
        this.gamesLost+=1;
    }

    public void incrementGamesDrawn() {
        this.gamesDrawn+=1;
    }
}
