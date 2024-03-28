package de.greensurvivors.findme.listener;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SignListener implements Listener {
    private static SignListener instance;

    private SignListener () {}

    public static SignListener inst() {
        if (instance == null) {
            instance = new SignListener();
        }
        return instance;
    }

    /**
     * join a findMe! game, if the 2nd line is [fm join] and 3rd line is the name of a game or
     * quit a findMe! game if the 2nd line is [fm quit]
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSignRightClick(PlayerInteractEvent event){
        if (event.useInteractedBlock() == Event.Result.DENY){
            return;
        }

        Player ePlayer = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand().equals(EquipmentSlot.HAND)){
            Block block = event.getClickedBlock();

            if (block.getState() instanceof Sign sign){
                boolean foundGame = false;
                SignSide frontSide = sign.getSide(Side.FRONT);

                if (Lang.SIGN_JOIN.get().equalsIgnoreCase(LegacyComponentSerializer.legacyAmpersand().serialize(frontSide.line(1)))){
                    sign.setWaxed(true);
                    Game gameOfPlayer = GameManager.inst().getGameOfPlayer(ePlayer);
                    if (gameOfPlayer != null){
                        ePlayer.sendMessage(Lang.build(Lang.ALREADY_IN_GAME_SELF.get().replace(Lang.VALUE, gameOfPlayer.getName())));
                        return;
                    }

                    String line3 = PlainTextComponentSerializer.plainText().serialize(frontSide.line(2));
                    for (String gameName : GameManager.inst().getGameNames()){
                        if (gameName.equalsIgnoreCase(line3)){
                            foundGame = true;

                            if (PermissionUtils.hasPermission(ePlayer, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
                                Game game = GameManager.inst().getGame(gameName);

                                if (!game.isAllowLateJoin() && game.getGameState().equals(Game.GameStates.ACTIVE)){
                                    ePlayer.sendMessage(Lang.build(Lang.GAME_ALREADY_ACTIVE.get()));
                                } else {
                                    GameManager.inst().playerJoinGame(ePlayer, game);
                                }

                            } else {
                                ePlayer.sendMessage(Lang.build(Lang.NO_PERMISSION_SOMETHING.get()));
                            }
                            break;
                        }
                    }

                    if (!foundGame){
                        ePlayer.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, line3)));
                    }

                } else if (Lang.SIGN_QUIT.get().equalsIgnoreCase(LegacyComponentSerializer.legacyAmpersand().serialize(frontSide.line(1)))){
                    sign.setWaxed(true);
                    Game gameOfPlayer = GameManager.inst().getGameOfPlayer(ePlayer);
                    if (gameOfPlayer != null){
                        GameManager.inst().playerQuitGame(ePlayer, gameOfPlayer);

                        ePlayer.sendMessage(Lang.build(Lang.QUIT_SELF.get()));
                    } else {
                        ePlayer.sendMessage(Lang.build(Lang.NOT_IN_GAME.get()));
                    }
                }
            }
        }
    }
}
