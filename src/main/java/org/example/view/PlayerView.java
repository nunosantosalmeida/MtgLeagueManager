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
    private Integer playerId;
    private String name;
    private String email;
    private String decklist;
    private String dateRegistered;
    private Integer rank;  // TODO on next iteration of testing this should be a primitive type
    private String points;
    private boolean isPresent;

    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int gamesDrawn;

    @Override
    public String toString() {
        return this.getName();
    }
}