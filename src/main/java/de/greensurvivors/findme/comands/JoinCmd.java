package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class JoinCmd { //todo join another player
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
            if (args.length >= 2){
                if (cs instanceof Player player) {
                        Game gameOfPlayer = GameManager.inst().getGameOfPlayer(player);

                        if (gameOfPlayer != null){
                            cs.sendMessage(Lang.build(Lang.ALREADY_IN_GAME.get().replace(Lang.VALUE, gameOfPlayer.getName())));
                        } else {
                            Game gameToJoin = GameManager.inst().getGame(args[1]);

                            if (gameToJoin != null){
                                GameManager.inst().playerJoinGame(player, gameToJoin);
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[1])));
                            }
                        }
                } else {
                    cs.sendMessage(Lang.NO_PLAYER.get());
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        return null;
    }
}
