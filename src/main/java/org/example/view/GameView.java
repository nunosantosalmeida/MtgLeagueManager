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
public class GameView {
    private Long gameId;
    @Getter
    private String gameDate;

    @Builder.Default
    private boolean isFinished = false;

    @Builder.Default
    private String gameResult = "";

    private String player1;
    private Long player1id;
    private String player2;
    private Long player2id;
    private String player3;
    private Long player3id;
    private String player4;
    private Long player4id;
    private String player5;
    private Long player5id;

    @Override
    public String toString() {
        return "["
                + player1 + ", "
                + player2 + ", "
                + player3 + ", "
                + (player4 != null ? player4 + ", " : "")
                + (player5 != null ? player5 + ", " : "")
                + "]";
    }
}