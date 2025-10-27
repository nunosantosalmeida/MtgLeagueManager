package org.example.configs;

import lombok.Getter;

@Getter
public class Configs {
    public static final Integer STARTING_POINTS = 1500;
    public static final double PENALTY_MULTIPLIER = 0.07;
    public static final double LOSS_MULTIPLIER = 0.07;
    public static final String DATE_FORMAT = "yyyy/MM/dd";
    public static final String TIME_FORMAT = "HH:mm";;
    public static final float DRAW_PERCENT_UPGRADE = 0.07F;
}
