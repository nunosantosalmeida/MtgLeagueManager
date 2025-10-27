package org.example.mapper;

import static org.example.configs.Configs.DATE_FORMAT;
import static org.example.configs.Configs.TIME_FORMAT;
import org.example.model.Game;
import org.example.model.Player;
import org.example.view.GameView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "cdi")
public interface GameMapper {

    //GameMapper gameMapper = Mappers.getMapper(GameMapper.class);

    @Mapping(target = "player1", expression = "java(getPlayerName(game.getGamePlayers(), 0))")
    @Mapping(target = "player1id", expression = "java(getPlayerId(game.getGamePlayers(), 0))")
    @Mapping(target = "player2", expression = "java(getPlayerName(game.getGamePlayers(), 1))")
    @Mapping(target = "player2id", expression = "java(getPlayerId(game.getGamePlayers(), 1))")
    @Mapping(target = "player3", expression = "java(getPlayerName(game.getGamePlayers(), 2))")
    @Mapping(target = "player3id", expression = "java(getPlayerId(game.getGamePlayers(), 2))")
    @Mapping(target = "player4", expression = "java(getPlayerName(game.getGamePlayers(), 3))")
    @Mapping(target = "player4id", expression = "java(getPlayerId(game.getGamePlayers(), 3))")
    @Mapping(target = "player5", expression = "java(getPlayerName(game.getGamePlayers(), 4))")
    @Mapping(target = "player5id", expression = "java(getPlayerId(game.getGamePlayers(), 4))")
    @Mapping(target = "gameId", source = "gameId") // Direct mapping
    @Mapping(target = "date", source = ".", qualifiedByName = "formatGameDate") // Custom method
    @Mapping(target = "result", source = ".", qualifiedByName = "getGameResult") // Custom method
    @Mapping(target = "isFinished", source = "finished") // Direct mapping
    GameView toGameView(Game game);

    default String getPlayerName(List<Player> players, int index) {
        return (players.size() > index) ? players.get(index).getName() : null;
    }

    default Integer getPlayerId(List<Player> players, int index) {
        return Math.toIntExact((players.size() > index) ? players.get(index).getPlayerId() : null);
    }

    @Named("formatGameDate")
    default String formatGameDate(Game game) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        return game.getDate().toLocalDate().format(dateFormatter) + " - " +
                game.getDate().toLocalTime().format(timeFormatter);
    }

    @Named("getGameResult")
    default String getGameResult(Game game) {
        if (game.isDraw()) {
            return "Draw";
        }
        if (game.getResult() == null || game.getResult().isEmpty()) {
            return "";
        }
        return game.getResult();
    }
}
