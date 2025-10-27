package org.example.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlayerView {
    private Long playerId;
    private String name;
    private String email;
    private String decklist;
    private String dateRegistered;
    private Integer rank;
    private String points;
    private Boolean isPresent;

    private Integer gamesPlayed;
    private Integer gamesWon;
    private Integer gamesLost;
    private Integer gamesDrawn;

    @Override
    public String toString() {
        return this.getName();
    }
}