package de.greensurvivors.greenfinder.listener;

import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SignListener implements Listener {
    private static SignListener instance;

    public static SignListener inst() {
        if (instance == null) {
            instance = new SignListener();
        }
        return instance;
    }

    @EventHandler
    private void onSignRightClick(PlayerInteractEvent event){
        if (event.useInteractedBlock() == Event.Result.DENY){
            return;
        }

        Player ePlayer = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand().equals(EquipmentSlot.HAND)){
            Block block = event.getClickedBlock();

            if (block.getState() instanceof Sign sign){
                boolean foundGame = false;

                for (String gameName : GameManager.inst().getGameNames()){
                    if (gameName.equalsIgnoreCase(LegacyComponentSerializer.legacyAmpersand().serialize(sign.line(1)))){
                        foundGame = true;

                        Game game = GameManager.inst().getGame(gameName);

                        if (!game.isAllowLateJoin() && game.getGameState().equals(Game.GameStates.ACTIVE)){
                            ePlayer.sendMessage("this game is already active.");
                        } else {
                            game.playerJoin(ePlayer);
                        }
                        break;
                    }
                }

                if (!foundGame){
                    ePlayer.sendMessage(Lang.build("Game not found."));
                }
            }
        }
    }
}
