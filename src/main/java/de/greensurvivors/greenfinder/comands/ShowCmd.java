package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class ShowCmd { //todo optional range parameter
    public static void handleCmd(CommandSender cs, String[] args) {
        if (cs instanceof LivingEntity livingEntity){
            if (args.length >= 3){
                Game game = GameManager.inst().getGame(args[2]);

                if (game != null){
                    //get the entity glowing in sync
                    Bukkit.getScheduler().runTask(GreenFinder.inst(), () ->
                            game.showAroundLocation(livingEntity.getLocation(), 20));
                } else {
                    cs.sendMessage(Lang.build("Unknown game."));

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
