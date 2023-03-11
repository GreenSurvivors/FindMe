package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

public class ShowCmd { //gamename
    public static void handleCmd(CommandSender cs, String[] args) {
        if (cs instanceof LivingEntity livingEntity){
            if (args.length >= 3){
                Game game = GameManager.inst().getGame(args[2]);

                if (game != null){
                    //get the entity glowing in sync
                    Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
                        game.showAroundLocation(livingEntity.getLocation(), 20);
                    });
                } else {
                    //unknown game

                }
            }
        }
    }
}
