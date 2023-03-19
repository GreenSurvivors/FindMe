package de.greensurvivors.findme.listener;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.dataObjects.HiddenStand;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

public class SlimeListener implements Listener {
    private static SlimeListener instance;

    public static SlimeListener inst() {
        if (instance == null) {
            instance = new SlimeListener();
        }
        return instance;
    }

    /**
     * if a slime gets right-clicked, of a player in a game, the hidden stand will get found
     */
    @EventHandler(ignoreCancelled = true)
    private void onSlimeClicked(PlayerInteractEntityEvent event){
        if (event.getHand() == EquipmentSlot.HAND){
            Entity rightClicked = event.getRightClicked();

            if (rightClicked instanceof Slime slime){
                if (slime.getPersistentDataContainer().get(HiddenStand.HIDDEN_KEY, PersistentDataType.STRING) != null){
                    event.setCancelled(true);

                    Player ePlayer = event.getPlayer();
                    if (PermissionUtils.hasPermission(ePlayer, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
                        Game game = GameManager.inst().getGameOfPlayer(ePlayer);

                        if (game != null && game.getGameState() == Game.GameStates.ACTIVE) {
                            game.findStand(ePlayer, slime.getUniqueId());
                        }
                    } else {
                        ePlayer.sendMessage(Lang.build(Lang.NO_PERMISSION_SOMETHING.get()));
                    }
                }
            }
        }
    }
}
