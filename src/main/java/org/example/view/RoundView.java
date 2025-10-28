package org.example.view;

import org.example.model.Game;
import org.example.model.Player;

import java.util.List;

@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter
public class RoundView {


    private Long round_id;
    private String round_date;
    private List<Game> round_games;
    private Boolean isRoundFinished = false;

    private List<Player> playersNotPresent = java.util.List.of();



    @Override
    public String toString() {
        StringBuilder roundGamesText = new StringBuilder();
        for (Game roundGame : round_games) {
            roundGamesText.append(roundGame.toString()).append("<br/>");
        }
        return roundGamesText.toString();
    }

}
