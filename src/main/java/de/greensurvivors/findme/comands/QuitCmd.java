package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class QuitCmd {
    /**
     * /gf quit - quit the game the command sender is playing
     * /gf quit <player name> - force a player to quit the findMe! game
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 2) { //other player
            if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_OTHER_PLAYERS)){
                Player otherPlayer = Bukkit.getPlayer(args[1]);
                if (otherPlayer != null){
                    Game game = GameManager.inst().getGameOfPlayer(otherPlayer);

                    if (game != null){
                        GameManager.inst().playerQuitGame(otherPlayer, game);
                        cs.sendMessage(Lang.build(Lang.QUIT_OTHER.get()));
                    } else {
                        cs.sendMessage(Lang.build(Lang.PLAYER_NOT_INGAME.get().replace(Lang.VALUE, otherPlayer.getName())));
                    }
                } else {
                    cs.sendMessage(Lang.build(Lang.PLAYER_NOT_ONLINE.get().replace(Lang.VALUE, args[1])));
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
            }
        } else if (cs instanceof Player player) { //self
            //this has no permission check, so a player can always quit.
            Game game = GameManager.inst().getGameOfPlayer(player);

            if (game != null){
                GameManager.inst().playerQuitGame(player, game);
            } else {
                cs.sendMessage(Lang.NOT_IN_GAME.get());
            }
        } else {
            cs.sendMessage(Lang.NO_PLAYER.get());
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        return null;
    }
}
