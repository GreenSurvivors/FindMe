package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
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
            HIDEAWAY_LONG = "hideaway", HIDEAWAY_SHORT = "hide",
            SIGN = "sign";
    /**
     * creates new games, hiding places and join signs
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                //fm c game <name>
                case GAME -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_GAME)){
                        if (GameManager.inst().addGame(args[2])){
                            //success
                            cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_CREATED.get().replace(Lang.VALUE, GAME)));
                        } else {
                            //unsuccessful
                            cs.sendMessage(Lang.build(Lang.GAME_ALREADY_EXISTS.get().replace(Lang.VALUE, args[2])));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm c hide <name>
                case HIDEAWAY_LONG, HIDEAWAY_SHORT -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_HIDEAWAY)){
                        if (cs instanceof LivingEntity livingEntity){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                //summon the entitys
                                game.addHideaway(livingEntity.getLocation());

                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_CREATED.get().replace(Lang.VALUE, HIDEAWAY_LONG)));
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
                //fm c sign <name>
                case SIGN -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_HIDEAWAY)){
                        if (cs instanceof LivingEntity livingEntity){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                //set the sign text

                                Block block = getSignLookingAt(livingEntity);

                                if (block != null){
                                    Sign sign = (Sign)block.getState();
                                    sign.line(1, Lang.build(Lang.SIGN_JOIN.get()));
                                    sign.line(2, Lang.build(game.getName()));

                                    sign.update();

                                    cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_CREATED.get().replace(Lang.VALUE, SIGN)));
                                } else {
                                    cs.sendMessage(Lang.build(Lang.NO_SIGN.get()));
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

    public static List<String> handleTap(CommandSender cs, String[] args) {
        switch (args.length){
            case 2 -> {
                //test for permission before adding suggestions
                List<String> result = new ArrayList<>();
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_GAME)){
                    result.add(GAME);
                }
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_SIGN)){
                    result.add(SIGN);
                }
                if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_HIDEAWAY)){
                    result.add(HIDEAWAY_LONG);
                    result.add(HIDEAWAY_SHORT);
                }
                return result.stream().filter(s -> s.toLowerCase().startsWith(args[1])).collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase(HIDEAWAY_LONG) || args[1].equalsIgnoreCase(HIDEAWAY_SHORT) || args[1].equalsIgnoreCase(SIGN)){
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_SIGN, PermissionUtils.FINDME_CREATE_HIDEAWAY)){
                        return GameManager.inst().getGameNames().stream().filter(s -> s.toLowerCase().startsWith(args[2])).collect(Collectors.toList());
                    }
                }
            }
        }
        return List.of();
    }
    /**
     * Get location of a sign a living entity is looking at.
     * @param entity looking at a sign
     * @return location of sign, or null if the entity is not looking at a sign
     */
    private static Block getSignLookingAt(LivingEntity entity) {
        // player is looking at a sign
        Block block = entity.getTargetBlockExact(5);

        if (block != null && Tag.ALL_SIGNS.isTagged(block.getType()))  {
            return block;
        } else {
            return null;
        }
    }
}
