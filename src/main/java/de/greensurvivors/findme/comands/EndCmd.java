package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.Findme;
import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class EndCmd {
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_END)){
            if (args.length >= 2){
                Game game = GameManager.inst().getGame(args[1]);

                if (game != null){
                    //set the sign text in sync
                    Bukkit.getScheduler().runTask(Findme.inst(), game::end);

                    cs.sendMessage(Lang.build("ending the game."));
                } else {
                    //no game by this name exits
                    cs.sendMessage(Lang.build("Unknown game"));
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
            }
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        //todo
        return null;
    }
}
