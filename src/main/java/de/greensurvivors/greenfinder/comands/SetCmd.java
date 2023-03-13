package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.dataObjects.TimeHelper;
import de.greensurvivors.greenfinder.language.Lang;
import de.greensurvivors.greenfinder.listener.InventoryListener;
import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class SetCmd {
    private static final String
            HEADS = "heads",
            GAME_LENGTH = "length",
            LATE_JOIN_LONG = "latejoin", LATE_JOIN_SHORT = "late",
            STARTPOINT_LONG = "startpoint", STARTPOINT_SHORT = "start",
            LOBBY = "lobby",
            ENDPOINT_LONG = "endpoint", ENDPOINT_SHORT = "end", ENDPOINT_ALT = "quit";

    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                case HEADS -> {
                    if (cs instanceof Player player) {
                        if (GameManager.inst().getGame(args[2]) == null){
                            cs.sendMessage("no game");
                            return;
                        }

                        InventoryListener.inst().OpenInventory(args[2], player);
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                    }
                }
                case GAME_LENGTH -> {
                    if (args.length >= 4){
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game == null){
                            cs.sendMessage("no game");
                            return;
                        } else {
                            long ticks = 0;
                            for (int i = 3; i < args.length; i++){
                                ticks += (new TimeHelper(args[i])).getTicks();
                            }

                            game.setGameTimeLength(ticks);
                            cs.sendMessage("successfully set to" + ticks + "ticks");
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                    }
                }
                case LATE_JOIN_SHORT, LATE_JOIN_LONG -> {
                    if (args.length >= 4){
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game == null){
                            cs.sendMessage("no game");
                        } else {
                            Boolean lateJoinAllowed = BooleanUtils.toBooleanObject(args[3]);
                            if (lateJoinAllowed != null){
                                game.setAllowLateJoin(lateJoinAllowed);

                                cs.sendMessage("set successful");
                            } else {
                             cs.sendMessage(Lang.build(Lang.NO_BOOL.get().replace(Lang.VALUE, args[3])));
                            }
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                    }
                }
                case STARTPOINT_SHORT, STARTPOINT_LONG -> {
                    if (cs instanceof LivingEntity livingEntity) {
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game == null){
                            cs.sendMessage("no game");
                            return;
                        }

                        game.setStartLoc(livingEntity.getLocation());
                        cs.sendMessage("set starting position successfully");
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                    }
                }
                case LOBBY -> {
                    if (cs instanceof LivingEntity livingEntity) {
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game == null){
                            cs.sendMessage("no game");
                            return;
                        }

                        game.setLobbyLoc (livingEntity.getLocation());
                        cs.sendMessage("set lobby position successfully");
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                    }
                }
                case ENDPOINT_SHORT, ENDPOINT_LONG, ENDPOINT_ALT -> {
                    if (cs instanceof LivingEntity livingEntity) {
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game == null){
                            cs.sendMessage("no game");
                            return;
                        }

                        game.setQuitLoc(livingEntity.getLocation());
                        cs.sendMessage("set quit position successfully");
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                    }
                }
                default -> cs.sendMessage(Lang.build(Lang.UNKNOWN_ARGUMENT.get()));
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        return null;
    }
}
