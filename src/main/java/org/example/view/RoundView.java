package org.example.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.Game;
import org.example.model.Player;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoundView {

    private Long roundId;
    private String date;
    private List<Game> games;
    @Builder.Default
    private Boolean isRoundFinished = false;
    @Builder.Default
    private List<Player> playersNotPresent = java.util.List.of();



    @Override
    public String toString() {
        StringBuilder roundGamesText = new StringBuilder();
        for (Game roundGame : games) {
            roundGamesText.append(roundGame.toString()).append("<br/>");
        }
        return roundGamesText.toString();
    }

}
