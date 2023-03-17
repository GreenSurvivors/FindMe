package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.PermissionUtils;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class StartCmd {
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDER_ADMIN, PermissionUtils.FINDER_START)){
            if (args.length >= 2){
                Game game = GameManager.inst().getGame(args[1]);

                if (game != null){
                    //set the sign text in sync
                    Bukkit.getScheduler().runTask(GreenFinder.inst(), game::startup);

                    cs.sendMessage(Lang.build("starting the game."));
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
