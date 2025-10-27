package org.example.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class GameView {
    private Integer gameId;
    @Getter
    private String date;

    @Builder.Default
    private boolean isFinished = false;

    @Builder.Default
    private String result = "";

    private String player1;
    private Integer player1id;
    private String player2;
    private Integer player2id;
    private String player3;
    private Integer player3id;
    private String player4;
    private Integer player4id;
    private String player5;
    private Integer player5id;

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