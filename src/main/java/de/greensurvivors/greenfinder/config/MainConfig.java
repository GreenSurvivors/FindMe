package de.greensurvivors.greenfinder.config;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.GreenLogger;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
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

    public static MainConfig inst() {
        if (instance == null) {
            instance = new MainConfig();
        }
        return instance;
    }

    /**
     *
     * @param name
     */
    public void removeGame(String name) {
        GameConfig gameConfig = new GameConfig(name);
        gameConfig.removeCfg();
    }

    public void saveGame(Game game) {
        GameConfig gameConfig = new GameConfig(game.getName());
        FileConfiguration fcfg = gameConfig.getCfg();

        fcfg.set("game", game.serialize());

        gameConfig.saveCfg();
    }

    /**
     * Load language configuration.
     */
    private void loadLanguage() {
        File file = new File(GreenFinder.inst().getDataFolder(), "language.yml");
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
                GreenFinder.inst().getName(),
                GreenFinder.inst().getDescription().getVersion())));
        cfg.options().parseComments(true);
        try {
            cfg.save(file);
        } catch (IOException e) {
            GreenLogger.log(Level.SEVERE, "Could not save language configuration.", e);
        }
    }

    public void reloadMain() {
        loadLanguage();

        loadGamesSave();
    }

    private void loadAllGames(){
        File gamesMotherFolder = new File(GreenFinder.inst().getDataFolder(), GameConfig.FOLDER);
        File[] gameFiles = gamesMotherFolder.listFiles();

        if (gameFiles != null){
            for (File gameFile : gameFiles){
                if (gameFile.isFile()){
                    GameConfig gameConfig = new GameConfig(FilenameUtils.removeExtension(gameFile.getName()));
                    FileConfiguration fcfg = gameConfig.getCfg();

                    MemorySection memorySection = ((MemorySection)fcfg.get("game"));
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
        Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
            // clear cache
            GameManager.inst().clearAll();
            loadAllGames();
        });
    }
}
