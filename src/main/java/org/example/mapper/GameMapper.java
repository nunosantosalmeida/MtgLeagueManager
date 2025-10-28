package org.example.mapper;

import static java.lang.Integer.parseInt;
import static org.example.Configs.DATE_FORMAT;
import static org.example.Configs.TIME_FORMAT;
import org.example.model.Game;
import org.example.model.Player;
import org.example.view.GameView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "cdi")
public interface GameMapper {

    @Mapping(target = "gameId", source = "gameId") // Direct mapping
    @Mapping(target = "date", source = ".", qualifiedByName = "formatGameDate") // Custom method
    @Mapping(target = "result", source = ".", qualifiedByName = "getGameResult") // Custom method
    @Mapping(target = "isFinished", source = "finished") // Direct mapping
    @Mapping(target = "player1", expression = "java(getPlayerName(game.getPlayers(), 0))")
    @Mapping(target = "player1id", expression = "java(getPlayerId(game.getPlayers(), 0))")
    @Mapping(target = "player2", expression = "java(getPlayerName(game.getPlayers(), 1))")
    @Mapping(target = "player2id", expression = "java(getPlayerId(game.getPlayers(), 1))")
    @Mapping(target = "player3", expression = "java(getPlayerName(game.getPlayers(), 2))")
    @Mapping(target = "player3id", expression = "java(getPlayerId(game.getPlayers(), 2))")
    @Mapping(target = "player4", expression = "java(getPlayerName(game.getPlayers(), 3))")
    @Mapping(target = "player4id", expression = "java(getPlayerId(game.getPlayers(), 3))")
    @Mapping(target = "player5", expression = "java(getPlayerName(game.getPlayers(), 4))")
    @Mapping(target = "player5id", expression = "java(getPlayerId(game.getPlayers(), 4))")
    GameView toGameView(final Game game);

    List<GameView> toListGameViews(final List<Game> listGames);

    default String getPlayerName(final List<Player> players, final int index) {
        return (players.size() > index) ? players.get(index).getName() : null;
    }

    default Integer getPlayerId(final List<Player> players, int index) {
        return (players.size() > index) ? players.get(index).getPlayerId() : null;
    }

    @Named("formatGameDate")
    default String formatGameDate(final Game game) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        return game.getDate().toLocalDate().format(dateFormatter) + " - " +
                game.getDate().toLocalTime().format(timeFormatter);
    }

    @Named("getGameResult")
    default String getGameResult(final Game game) {
        if (game.isDraw()) {
            return "Draw";
        }
        if (game.getResult() == null || game.getResult().isEmpty()) {
            return "";
        }

        final Optional<Player> winnerId = game.getPlayers().stream().filter(p -> p.getPlayerId().equals(parseInt(game.getResult()))).findFirst();

        return winnerId.isPresent() ? winnerId.get().getName() : "";
    }
}
