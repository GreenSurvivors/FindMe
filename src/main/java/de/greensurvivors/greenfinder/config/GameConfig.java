package de.greensurvivors.greenfinder.config;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.GreenLogger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

public class GameConfig {
    protected final static String FOLDER = "games";

    private final String path;
    private final String fileName;

    private YamlConfiguration cfg = null;

    public GameConfig(String gameName){
        // for some reason linux doesn't handle '-' very well in folderNames.
        this.path = (FOLDER + File.separator).replace("-", "_");
        fileName = gameName + ".yml";
    }

    /**
     * remove config file
     */
    public boolean removeCfg(){
        File file = new File(GreenFinder.inst().getDataFolder(), path + fileName);
        return file.delete();
    }

    /**
     * Save configuration to file.
     */
    public void saveCfg() {
        File file = new File(GreenFinder.inst().getDataFolder(), path + fileName);

        //make sure the cfg was loaded
        if (cfg == null) {
            cfg = YamlConfiguration.loadConfiguration(file);
        }

        // save modified configuration
        cfg.options().setHeader(Collections.singletonList(String.format(
                fileName.replace(" .yml", "").replace("_", " ") + " playerFile for %s (%s)",
                GreenFinder.inst().getName(),
                GreenFinder.inst().getDescription().getVersion())));
        cfg.options().parseComments(true);
        try {
            cfg.save(file);
        } catch (IOException e) {
            GreenLogger.log(Level.SEVERE, "Could not save " + fileName + " player file.", e);
        }
    }

    /**
     * get the config, if null was the last value try to load
     * @return cfg
     */
    public YamlConfiguration getCfg() {
        if (cfg == null) {
            File file = new File(GreenFinder.inst().getDataFolder(), path + fileName);
            cfg = YamlConfiguration.loadConfiguration(file);
        }
        return cfg;
    }
}
