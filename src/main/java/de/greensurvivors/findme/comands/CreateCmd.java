package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.Findme;
import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateCmd {
    private static final String
            GAME = "game",
            STAND = "stand",
            SIGN = "sign";

    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                //fm c game <name>
                case GAME -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_GAME)){
                        Bukkit.getScheduler().runTask(Findme.inst(), () -> {
                            if (GameManager.inst().addGame(args[2])){
                                //success
                                cs.sendMessage(Lang.build("success"));
                            } else {
                                //unsuccessful
                                cs.sendMessage(Lang.build("game already exits"));
                            }
                        });
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm c stand <name>
                case STAND -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_STAND)){
                        if (cs instanceof LivingEntity livingEntity){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                //summon the entity in sync
                                Bukkit.getScheduler().runTask(Findme.inst(), () -> {
                                game.addHiddenStand(livingEntity.getLocation());
                                });

                                cs.sendMessage(Lang.build("created new stand."));
                            } else {
                                //no game by this name exits
                                cs.sendMessage(Lang.build("Unknown game"));
                            }
                        } else {
                            //no location of cs
                            cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                case SIGN -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_STAND)){
                        if (cs instanceof LivingEntity livingEntity){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                //set the sign text in sync
                                Bukkit.getScheduler().runTask(Findme.inst(), () -> {
                                    Sign sign = getSignLookingAt(livingEntity);

                                    if (sign != null){
                                        sign.line(1, Lang.build("[join fm]"));
                                        sign.line(2, Lang.build(game.getName()));

                                    } else {
                                        cs.sendMessage(Lang.build("Lang.NO_SIGN.get()"));
                                    }
                                });

                                cs.sendMessage(Lang.build("created new sign."));
                            } else {
                                //no game by this name exits
                                cs.sendMessage(Lang.build("Unknown game"));
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
                    cs.sendMessage(Lang.build("try /fm help"));
                }
            }
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        switch (args.length){
            case 2 -> {
                List<String> result = new ArrayList<>();
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_GAME)){
                    result.add(GAME);
                }
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_SIGN)){
                    result.add(SIGN);
                }
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_STAND)){
                    result.add(STAND);
                }
                return result.stream().filter(s -> args[1].toLowerCase().startsWith(s)).collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase(STAND) || args[1].equalsIgnoreCase(SIGN)){
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_SIGN, PermissionUtils.FINDME_CREATE_STAND)){
                        return GameManager.inst().getGameNames().stream().filter(s -> args[2].toLowerCase().startsWith(s)).collect(Collectors.toList());
                    }
                }
            }
        }
        return null;
    }
    /**
     * Get location of a sign a living entity is looking at.
     * @param entity looking at a sign
     * @return location of sign, or null if the entity is not looking at a sign
     */
    private static Sign getSignLookingAt(LivingEntity entity) {
        // player is looking at a sign
        Block block = entity.getTargetBlockExact(5);

        if (block != null && Tag.ALL_SIGNS.isTagged(block.getType()))  {
            return (Sign)block.getState();
        } else {
            return null;
        }
    }
}
