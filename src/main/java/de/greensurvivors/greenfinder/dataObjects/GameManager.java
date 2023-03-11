package de.greensurvivors.greenfinder.dataObjects;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class GameManager {
    private static GameManager instance;
    private final HashMap<String, Game> games = new HashMap<>(); //name of game to better find it

    private final HashMap<Player, Game> playersInGames = new HashMap<>();

    public static GameManager inst(){
        if (instance == null){
            instance = new GameManager();
        }

        return instance;
    }

    public Set<Game> getActiveGames(){
        return games.values().stream().filter(g -> g.getGameState() == Game.GameStates.ACTIVE).collect(Collectors.toSet());
    }

    /**
     *
     * @param name
     * @return true if the new game was created, false if a game by this game already exits
     */
    public boolean addGame(@NotNull String name){
        if (games.get(name) == null){
            return false;
        }

        games.put(name, new Game(name));
        return true;
    }

    public boolean removeGame(@NotNull String name){
        Game game = getGame(name);
        if (game != null){
            game.end();

            return games.remove(name) != null;
        } else {
            return false;
        }
    }

    public @Nullable Game getGameOfPlayer (@NotNull Player player){
        return playersInGames.get(player);
    }

    public @Nullable Game getGame (@NotNull String nameOfGame){
        return games.get(nameOfGame);
    }

    public @NotNull Set<String> getGameNames(){
        return games.keySet();
    }

    public void playerJoinGame(@NotNull Player player, @NotNull String nameofGame){
        Game game = getGame(nameofGame);

        if (game != null){
            playerJoinGame(player, game);
        }
    }

    public void playerJoinGame(@NotNull Player player, @NotNull Game game){
        game.playerJoin(player);
        playersInGames.put(player, game);

    }

    public void playerQuitGame(@NotNull Player player, @NotNull String nameofGame){
        Game game = getGame(nameofGame);

        if (game != null){
            playerQuitGame(player, game);
        }

    }

    public void playerQuitGame(@NotNull Player player, @NotNull Game game){
        game.playerQuit(player);
        playersInGames.remove(player);
    }

    public void clearAll(){
        for (Game game : games.values()){
            game.clear();
        }

        games.clear();
    }
}
