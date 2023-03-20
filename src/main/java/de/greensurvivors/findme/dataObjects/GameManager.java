package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.GreenLogger;
import de.greensurvivors.findme.config.MainConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GameManager {
    private static GameManager instance;
    private final HashMap<String, Game> games = new HashMap<>(); //name of game to better find it

    private final HashMap<Player, Game> playersInGames = new HashMap<>();
    private final HashSet<Player> teleportingPlayers = new HashSet<>();

    public static GameManager inst(){
        if (instance == null){
            instance = new GameManager();
        }

        return instance;
    }

    /**
     * get a set of all games that are in active state
     * @return
     */
    public Set<Game> getActiveGames(){
        return games.values().stream().filter(g -> g.getGameState() == Game.GameStates.ACTIVE).collect(Collectors.toSet());
    }

    /**
     * Please note, that the proper way to load all games from config is to call clearAll() first.
     * @param game
     */
    public void addLoadedGame(Game game){
        if (games.get(game.getName()) != null){
            GreenLogger.log(Level.WARNING, "loaded a already existing game, I will overwrite the old one.");
        }

        games.put(game.getName(), game);
    }

    /**
     *
     * @param name
     * @return true if the new game was created, false if a game by this game already exits
     */
    public boolean addGame(@NotNull String name){
        if (games.get(name) != null){
            return false;
        }

        games.put(name, new Game(name));
        return true;
    }

    /**
     * proper way to remove a findMe! game
     * @param name
     * @return
     */
    public boolean removeGame(@NotNull String name){
        Game game = getGame(name);
        if (game != null){
            game.end();
            game.removeAllHideaways();

            MainConfig.inst().removeGame(game.getName());

            return games.remove(name) != null;
        } else {
            return false;
        }
    }

    /**
     * get the game a player is in or null if they are not part of one
     * @param player
     * @return
     */
    public @Nullable Game getGameOfPlayer (@NotNull Player player){
        return playersInGames.get(player);
    }

    /**
     * get a game by its name
     * @param nameOfGame
     * @return
     */
    public @Nullable Game getGame (@NotNull String nameOfGame){
        return games.get(nameOfGame);
    }

    /**
     * get set of all game names
     * @return
     */
    public @NotNull Set<String> getGameNames(){
        return games.keySet();
    }

    /**
     * proper way for a player to join a game
     * @param player
     * @param game
     */
    public void playerJoinGame(@NotNull Player player, @NotNull Game game){
        game.playerJoin(player);
        playersInGames.put(player, game);

    }

    /**
     * way to quit a game if the game is unknown. relays on internal lookup, so use the sister function if the game is known
     * @param player
     */
    public void playerQuitGame(@NotNull Player player){
        Game game = playersInGames.get(player);

        if (game != null){
            game.playerQuit(player);
            playersInGames.remove(player);
        }
    }

    /**
     * called by the game if it ends
     * @param player
     */
    protected void playersGameEnded(@NotNull Player player){
        playersInGames.remove(player);
    }

    /**
     * way to quit a game if the game is known. use the sister function if the game is unknown
     * @param player
     */
    public void playerQuitGame(@NotNull Player player, @NotNull Game game){
        game.playerQuit(player);
        playersInGames.remove(player);
    }

    /**
     * clears all games and internal data. Only supposed to be called on shutdown.
     */
    public void clearAll(){
        for (Game game : games.values()){
            game.clear();
        }

        games.clear();
        playersInGames.clear();
    }

    protected void addTeleportingPlayer(@NotNull Player player){
        teleportingPlayers.add(player);
    }

    protected void removeTeleportingPlayer(@NotNull Player player){
        teleportingPlayers.remove(player);
    }

    /**
     * returns if the player is teleporting because the game demands it e.a. porting to a lobby
     * @param player
     * @return
     */
    public boolean isPlayerTeleportingGame(@NotNull Player player){
        return teleportingPlayers.contains(player);
    }
}
