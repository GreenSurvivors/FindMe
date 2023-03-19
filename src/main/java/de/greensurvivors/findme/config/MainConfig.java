package de.greensurvivors.findme.config;

import de.greensurvivors.findme.Findme;
import de.greensurvivors.findme.GreenLogger;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

public class MainConfig {

    private static MainConfig instance;

    private final static String GAME_KEY = "game";

    public static MainConfig inst() {
        if (instance == null) {
            instance = new MainConfig();
        }
        return instance;
    }

    /**
     * deletes a game file
     * @param name
     */
    public void removeGame(String name) {
        GameConfig gameConfig = new GameConfig(name);
        gameConfig.removeCfg();
    }

    /**
     * save a game to the disc
     * @param game
     */
    public void saveGame(Game game) {
        GameConfig gameConfig = new GameConfig(game.getName());
        FileConfiguration fcfg = gameConfig.getCfg();

        fcfg.set(GAME_KEY, game.serialize());

        gameConfig.saveCfg();
    }

    /**
     * Load language configuration.
     */
    private void loadLanguage() {
        File file = new File(Findme.inst().getDataFolder(), "language.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String k;
        // check Config
        for (Lang l : Lang.values()) {
            k = l.name().replaceAll("_", ".");
            // load value, if value not exist add
            if (cfg.contains(k))
                l.set(cfg.getString(k));
            else
                cfg.set(k, l.get());
        }
        // save modified configuration
        cfg.options().setHeader(Collections.singletonList(String.format(
                "Language configuration for %s (%s)",
                Findme.inst().getName(),
                Findme.inst().getDescription().getVersion())));
        cfg.options().parseComments(true);
        try {
            cfg.save(file);
        } catch (IOException e) {
            GreenLogger.log(Level.SEVERE, "Could not save language configuration.", e);
        }
    }

    /**
     * load language and all games
     */
    public void reloadAll() {
        loadLanguage();

        loadGamesSave();
    }

    /**
     * (re)load all games from disc
     */
    private void loadAllGames(){
        File gamesMotherFolder = new File(Findme.inst().getDataFolder(), GameConfig.FOLDER);
        File[] gameFiles = gamesMotherFolder.listFiles();

        if (gameFiles != null){
            for (File gameFile : gameFiles){
                if (gameFile.isFile()){
                    GameConfig gameConfig = new GameConfig(FilenameUtils.removeExtension(gameFile.getName()));
                    FileConfiguration fcfg = gameConfig.getCfg();

                    MemorySection memorySection = ((MemorySection)fcfg.get(GAME_KEY));
                    if (memorySection != null){
                        Game game = Game.deserialize(memorySection.getValues(false));

                        if (game != null){
                            GameManager.inst().addLoadedGame(game);
                        } else {
                            GreenLogger.log(Level.WARNING, "could not deserialize game in file " + gameFile.getName());
                        }
                    } else {
                        GreenLogger.log(Level.INFO, "no valid game data found in " + gameFile.getName() + ".");
                    }
                } else {
                    GreenLogger.log(Level.WARNING, gameFile + " is not a file.");
                }
            }
        } else {
            GreenLogger.log(Level.INFO, "No games to load.");
        }
    }

    /**
     * Load region restock and clears cached ones.
     */
    public void loadGamesSave() {
        Bukkit.getScheduler().runTask(Findme.inst(), () -> {
            // clear cache
            GameManager.inst().clearAll();
            loadAllGames();
        });
    }
}
