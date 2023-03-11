package de.greensurvivors.greenfinder;

import de.greensurvivors.greenfinder.comands.FinderCommands;
import de.greensurvivors.greenfinder.config.MainConfig;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.listener.ArmorstandListener;
import de.greensurvivors.greenfinder.listener.InventoryListener;
import de.greensurvivors.greenfinder.listener.SignListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GreenFinder extends JavaPlugin {
    private static GreenFinder instance;

    public static GreenFinder inst() {
        return instance;
    }

    @Override
    public void onEnable() {
        // set instance
        instance = this;

        // set logger
        GreenLogger.setLogger(getLogger());

        // configuration
        MainConfig.inst().reloadMain();

        // command
        getCommand(FinderCommands.CMD).setExecutor(FinderCommands.inst());

        // listener
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(ArmorstandListener.inst(), this);
        pm.registerEvents(InventoryListener.inst(), this);
        pm.registerEvents(SignListener.inst(), this);
    }

    @Override
    public void onDisable() {
        GameManager.inst().clearAll();
    }
}
