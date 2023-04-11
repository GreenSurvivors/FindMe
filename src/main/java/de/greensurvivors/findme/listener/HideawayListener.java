package de.greensurvivors.findme.listener;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.dataObjects.Hideaway;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

public class HideawayListener implements Listener {
    private static HideawayListener instance;

    private HideawayListener () {}

    public static HideawayListener inst() {
        if (instance == null) {
            instance = new HideawayListener();
        }
        return instance;
    }

    /**
     * if a hitBoxEntity gets right-clicked, of a player in a game, the hideaway will get found
     */
    @EventHandler(ignoreCancelled = true)
    private void onHitBoxClicked(PlayerInteractEntityEvent event){
        if (event.getHand() == EquipmentSlot.HAND){
            Entity rightClicked = event.getRightClicked();

            if (rightClicked instanceof Interaction){
                if (rightClicked.getPersistentDataContainer().get(Hideaway.HIDDEN_KEY, PersistentDataType.STRING) != null){
                    event.setCancelled(true);

                    Player ePlayer = event.getPlayer();
                    if (PermissionUtils.hasPermission(ePlayer, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
                        Game game = GameManager.inst().getGameOfPlayer(ePlayer);

                        if (game != null && game.getGameState() == Game.GameStates.ACTIVE) {
                            game.findHideaway(ePlayer, rightClicked.getUniqueId());
                        }
                    } else {
                        ePlayer.sendMessage(Lang.build(Lang.NO_PERMISSION_SOMETHING.get()));
                    }
                }
            }
        }
    }
}
