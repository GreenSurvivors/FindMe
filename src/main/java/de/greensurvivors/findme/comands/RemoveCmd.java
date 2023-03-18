package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.Findme;
import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveCmd {
    private static final String
            GAME = "game",
            STAND = "stand";

    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                //fm c game <name>
                case GAME -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_GAME)){
                        if (GameManager.inst().removeGame(args[2])){
                            //success
                            cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_REMOVED.get().replace(Lang.VALUE, GAME)));
                        } else {
                            //unsuccessful
                            cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm c stand <name>
                case STAND -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_STAND)){
                        if (cs instanceof LivingEntity livingEntity){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                //summon the entity in sync
                                Bukkit.getScheduler().runTask(Findme.inst(), () -> {
                                    game.removeHiddenStand(game.getNearestStand(livingEntity.getLocation()).getUniqueId());
                                });

                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_REMOVED.get().replace(Lang.VALUE, STAND)));
                            } else {
                                //no game by this name exits
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            //no location of cs
                            cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                default -> {
                    //didn't understand what should get created
                    cs.sendMessage(Lang.build(Lang.UNKNOWN_ARGUMENT.get().replace(Lang.VALUE, args[1])));
                }
            }
        }
    }

    public static java.util.List<String> handleTap(CommandSender cs, String[] args) {
        switch (args.length){
            case 2 -> {
                List<String> result = new ArrayList<>();
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_GAME)){
                    result.add(GAME);
                }
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_STAND)){
                    result.add(STAND);
                }
                return result.stream().filter(s -> args[1].toLowerCase().startsWith(s)).collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase(STAND) || args[1].equalsIgnoreCase(GAME)){
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_STAND, PermissionUtils.FINDME_REMOVE_GAME)){
                        return GameManager.inst().getGameNames().stream().filter(s -> args[2].toLowerCase().startsWith(s)).collect(Collectors.toList());
                    }
                }
            }
        }
        return null;
    }
}
