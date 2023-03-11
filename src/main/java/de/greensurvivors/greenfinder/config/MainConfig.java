package de.greensurvivors.greenfinder.config;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.GreenLogger;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
        GreenFinder.inst().reloadConfig();
        saveMain();
        loadMainSave();

        loadLanguage();
    }

    private void saveMain() {
        FileConfiguration cfg = GreenFinder.inst().getConfig();

        cfg.options().setHeader(List.of(GreenFinder.inst().getName() + " " + GreenFinder.inst().getDescription().getVersion()));
        cfg.options().copyDefaults(true);
        cfg.options().parseComments(true);
        GreenFinder.inst().saveConfig();
    }

    private void loadMain() {
        FileConfiguration cfg = GreenFinder.inst().getConfig();
    }

    /**
     * Load region restock and clears cached ones.
     */
    public void loadMainSave() {
        Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
            Bukkit.getScheduler().runTaskAsynchronously(GreenFinder.inst(), this::loadMain);
        });
    }
}
