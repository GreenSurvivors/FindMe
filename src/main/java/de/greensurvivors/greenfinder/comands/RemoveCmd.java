package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.PermissionUtils;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveCmd {//todo translations
    private static final String
            GAME = "game",
            STAND = "stand";

    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                //gf c game <name>
                case GAME -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDER_ADMIN, PermissionUtils.FINDER_REMOVE, PermissionUtils.FINDER_REMOVE_GAME)){
                        if (GameManager.inst().removeGame(args[2])){
                            //success
                            cs.sendMessage(Lang.build("success"));
                        } else {
                            //unsuccessful
                            cs.sendMessage(Lang.build("game does not exits"));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //gf c stand <name>
                case STAND -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDER_ADMIN, PermissionUtils.FINDER_REMOVE, PermissionUtils.FINDER_REMOVE_STAND)){
                        if (cs instanceof LivingEntity livingEntity){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                //summon the entity in sync
                                Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
                                    game.removeHiddenStand(game.getNearestStand(livingEntity.getLocation()).getUniqueId());
                                });

                                cs.sendMessage(Lang.build("removed stand."));
                            } else {
                                //no game by this name exits
                                cs.sendMessage(Lang.build("Unknown game"));
                            }
                        } else {
                            //no location of cs
                            cs.sendMessage(Lang.build("No entity"));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                default -> {
                    //didn't understand what should get created
                    cs.sendMessage(Lang.build("try /gf help"));
                }
            }
        }
    }

    public static java.util.List<String> handleTap(CommandSender cs, String[] args) {
        switch (args.length){
            case 2 -> {
                List<String> result = new ArrayList<>();
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDER_ADMIN, PermissionUtils.FINDER_REMOVE, PermissionUtils.FINDER_REMOVE_GAME)){
                    result.add(GAME);
                }
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDER_ADMIN, PermissionUtils.FINDER_REMOVE, PermissionUtils.FINDER_REMOVE_STAND)){
                    result.add(STAND);
                }
                return result.stream().filter(s -> args[1].toLowerCase().startsWith(s)).collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase(STAND) || args[1].equalsIgnoreCase(GAME)){
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDER_ADMIN, PermissionUtils.FINDER_REMOVE, PermissionUtils.FINDER_REMOVE_STAND, PermissionUtils.FINDER_REMOVE_GAME)){
                        return GameManager.inst().getGameNames().stream().filter(s -> args[2].toLowerCase().startsWith(s)).collect(Collectors.toList());
                    }
                }
            }
        }
        return null;
    }
}
