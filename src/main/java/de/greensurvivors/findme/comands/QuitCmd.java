package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class QuitCmd { //todo quit another player
    public static void handleCmd(CommandSender cs, String[] args) { //this has no permission check, so a player can always quit.
        if (cs instanceof Player player) {
            Game game = GameManager.inst().getGameOfPlayer(player);

            if (game == null){
                cs.sendMessage(Lang.NOT_IN_GAME.get());
            } else {
                GameManager.inst().playerQuitGame(player, game);
            }
        } else {
            cs.sendMessage(Lang.NO_PLAYER.get());
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        return null;
    }
}
