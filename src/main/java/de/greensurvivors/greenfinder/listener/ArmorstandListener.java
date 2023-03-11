package de.greensurvivors.greenfinder.listener;

import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.persistence.PersistentDataType;

public class ArmorstandListener implements Listener {
    private static ArmorstandListener instance;

    public static ArmorstandListener inst() {
        if (instance == null) {
            instance = new ArmorstandListener();
        }
        return instance;
    }


    @EventHandler(ignoreCancelled = true)
    private void onArmorStandClicked(PlayerArmorStandManipulateEvent event){
        ArmorStand armorStand = event.getRightClicked();

        if (armorStand.getPersistentDataContainer().get(Game.hiddenKey, PersistentDataType.INTEGER) != null){
            event.setCancelled(true);

            Player ePlayer = event.getPlayer();
            Game game = GameManager.inst().getGameOfPlayer(ePlayer);

            if (game != null && game.getGameState() == Game.GameStates.ACTIVE) {
                game.findStand(armorStand.getUniqueId());
            }
        }
    }
}
