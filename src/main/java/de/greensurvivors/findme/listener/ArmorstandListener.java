package de.greensurvivors.findme.listener;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.dataObjects.HiddenStand;
import de.greensurvivors.findme.language.Lang;
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

        if (armorStand.getPersistentDataContainer().get(HiddenStand.HIDDEN_KEY, PersistentDataType.INTEGER) != null){
            event.setCancelled(true);

            Player ePlayer = event.getPlayer();
            if (PermissionUtils.hasPermission(ePlayer, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
                Game game = GameManager.inst().getGameOfPlayer(ePlayer);

                if (game != null && game.getGameState() == Game.GameStates.ACTIVE) {
                    game.findStand(armorStand.getUniqueId());
                }
            } else {
                ePlayer.sendMessage(Lang.build(Lang.NO_PERMISSION_SOMETHING.get()));
            }
        }
    }
}
