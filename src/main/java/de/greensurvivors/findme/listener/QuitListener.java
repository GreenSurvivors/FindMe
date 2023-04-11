package de.greensurvivors.findme.listener;

import de.greensurvivors.findme.dataObjects.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener  implements Listener {
    private static QuitListener instance;

    private QuitListener () {}

    public static QuitListener inst() {
        if (instance == null) {
            instance = new QuitListener();
        }
        return instance;
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event){
        GameManager.inst().playerQuitGame(event.getPlayer());
    }

    //please note: this might change in the future, if a global game is needed.
    //however if this change happens, it will get a config value
    @EventHandler(ignoreCancelled = true)
    private void onWorldChange(PlayerChangedWorldEvent event){
        //don't quit the player if they are being ported by the game
        if (!GameManager.inst().isPlayerTeleportingGame(event.getPlayer())){
            GameManager.inst().playerQuitGame(event.getPlayer());
        }
    }
}
