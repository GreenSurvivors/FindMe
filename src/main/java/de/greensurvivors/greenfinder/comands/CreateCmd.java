package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class CreateCmd {//todo translations
    private static final String
            GAME = "game",
            STAND = "stand",
            SIGN = "sign";

    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                //gf c game <name>
                case GAME -> {
                    if (GameManager.inst().addGame(args[2])){
                        //success
                        cs.sendMessage(Lang.build("success"));
                    } else {
                        //unsuccessful
                        cs.sendMessage(Lang.build("game already exits"));
                    }
                }
                //gf c stand <name>
                case STAND -> {
                    if (cs instanceof LivingEntity livingEntity){
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game != null){
                            //summon the entity in sync
                            Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
                            game.addHiddenStand(livingEntity.getLocation());
                            });

                            cs.sendMessage(Lang.build("created new stand."));
                        } else {
                            //no game by this name exits
                            cs.sendMessage(Lang.build("Unknown game"));
                        }
                    } else {
                        //no location of cs

                        cs.sendMessage(Lang.build("No entity"));
                    }
                }
                case SIGN -> {
                    if (cs instanceof LivingEntity livingEntity){
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game != null){
                            //summon the entity in sync
                            Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
                                game.addHiddenStand(livingEntity.getLocation());
                            });

                            cs.sendMessage(Lang.build("created new stand."));
                        } else {
                            //no game by this name exits
                            cs.sendMessage(Lang.build("Unknown game"));
                        }
                    } else {
                        //no location of cs

                        cs.sendMessage(Lang.build("No entity"));
                    }
                }
                default -> {
                    //didn't understand what should get created
                    cs.sendMessage(Lang.build("try /help"));
                }
            }
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        switch (args.length){
            case 1 -> {
                return List.of(GAME, STAND);
            }
            case 2 -> {
                if (args[1].equalsIgnoreCase(STAND)){
                    return new ArrayList<>(GameManager.inst().getGameNames());
                }
            }
        }
        return null;
    }
}
