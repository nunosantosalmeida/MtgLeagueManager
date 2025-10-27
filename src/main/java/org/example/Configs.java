package org.example;

import lombok.Getter;

@Getter
public class Configs {
    public static final Integer STARTING_POINTS = 1500;

    public static final float PENALTY_MULTIPLIER = 0.93f;
    public static final float LOSS_MULTIPLIER = 0.93f;
    public static final float DRAW_POINTS_DISTRIBUTION_MULTIPLIER = 0.07F;

    public static final float THREE_PLAYER_POD_PONDERATION = 0.07F;
    public static final float FIVE_PLAYER_POD_WIN_PONDERATION = 0.8F;

    public static final String DATE_FORMAT = "yyyy/MM/dd";
    public static final String TIME_FORMAT = "HH:mm";;
}
