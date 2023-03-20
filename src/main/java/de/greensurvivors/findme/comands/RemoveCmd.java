package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.dataObjects.Hideaway;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveCmd {
    private static final String
            GAME = "game",
            HIDEAWAY_LONG = "hideaway", HIDEAWAY_SHORT = "hide";

    /**
     * delete a game or a hiding place
     * /fm rem game <game name>
     * /fm rem stand <game name> - removes the nearest hideaway
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) { //todo remove all hideaways with a game
        if (args.length >= 3){
            switch (args[1]){
                //fm rem game <game name>
                case GAME -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_GAME)){
                        if (GameManager.inst().removeGame(args[2])){
                            //successful
                            cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_REMOVED.get().replace(Lang.VALUE, GAME)));
                        } else {
                            //unsuccessful
                            cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm rem hide <game name>
                case HIDEAWAY_LONG, HIDEAWAY_SHORT -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_HIDEAWAY)){
                        if (cs instanceof LivingEntity livingEntity){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                //remove the entity
                                Hideaway hideaway = game.getNearestHideaway(livingEntity.getLocation());
                                if (hideaway != null){
                                    game.removeHideaway(hideaway.getUUIDSlime());
                                    cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_REMOVED.get().replace(Lang.VALUE, HIDEAWAY_LONG)));
                                } else {
                                    //no hideaways where found
                                    cs.sendMessage(Lang.build(Lang.NO_NEARBY_STAND.get()));
                                }
                            } else {
                                //no game by this name exits
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            //no location of cs
                            cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                        }
                    } else {
                        //no permission
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
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_HIDEAWAY)){
                    result.add(HIDEAWAY_LONG);
                    result.add(HIDEAWAY_SHORT);
                }
                return result.stream().filter(s -> s.toLowerCase().startsWith(args[1])).collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase(HIDEAWAY_LONG) || args[1].equalsIgnoreCase(HIDEAWAY_SHORT) || args[1].equalsIgnoreCase(GAME)){
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_HIDEAWAY, PermissionUtils.FINDME_REMOVE_GAME)){
                        return GameManager.inst().getGameNames().stream().filter(s -> s.toLowerCase().startsWith(args[2])).collect(Collectors.toList());
                    }
                }
            }
        }
        return null;
    }
}
