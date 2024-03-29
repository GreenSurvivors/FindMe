package de.greensurvivors.findme;

import de.greensurvivors.findme.comands.FindMeCommands;
import de.greensurvivors.findme.config.MainConfig;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.listener.HideawayListener;
import de.greensurvivors.findme.listener.InventoryListener;
import de.greensurvivors.findme.listener.QuitListener;
import de.greensurvivors.findme.listener.SignListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FindMe extends JavaPlugin {
    private static FindMe instance;

    public static FindMe inst() {
        return instance;
    }

    @Override
    public void onEnable() {
        // set instance
        instance = this;

        // set logger
        GreenLogger.setLogger(getLogger());

        // configuration
        MainConfig.inst().reloadAll();

        // commands
        getCommand(FindMeCommands.CMD).setExecutor(FindMeCommands.inst());

        // listener
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(HideawayListener.inst(), this);
        pm.registerEvents(InventoryListener.inst(), this);
        pm.registerEvents(SignListener.inst(), this);
        pm.registerEvents(QuitListener.inst(), this);
    }

    @Override
    public void onDisable() {
        GameManager.inst().clearAll();
    }
}
