package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.Findme;
import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class ShowCmd { //todo optional range parameter
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SHOW)){
            if (cs instanceof LivingEntity livingEntity){
                if (args.length >= 3){
                    Game game = GameManager.inst().getGame(args[2]);

                    if (game != null){
                        //get the entity glowing in sync
                        Bukkit.getScheduler().runTask(Findme.inst(), () ->
                                game.showAroundLocation(livingEntity.getLocation(), 20));
                    } else {
                        cs.sendMessage(Lang.build("Unknown game."));

                    }
                } else {
                    cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        //todo
        return null;
    }
}
