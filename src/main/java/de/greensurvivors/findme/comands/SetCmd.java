package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.Utils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.dataObjects.TimeHelper;
import de.greensurvivors.findme.language.Lang;
import de.greensurvivors.findme.listener.InventoryListener;
import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SetCmd {
    private static final String
            HEADS = "heads",
            GAME_LENGTH = "length",
            LATE_JOIN_LONG = "latejoin", LATE_JOIN_SHORT = "late",
            STARTPOINT_LONG = "startpoint", STARTPOINT_SHORT = "start",
            LOBBY = "lobby",
            ENDPOINT_LONG = "endpoint", ENDPOINT_SHORT = "end", ENDPOINT_ALT = "quit",
            STARTING_PERCENT_LONG = "starting_hidden_percent", STARTING_PERCENT_SHORT = "shp",
            AVERAGE_TICKS_UNTIL_REHEAD_LONG = "average_ticks_until_rehead", AVERAGE_TICKS_UNTIL_REHEAD_SHORT = "atr",
            REHEAD_COOLDOWN_LONG = "rehead_cooldown", REHEAD_COOLDOWN_SHORT = "cooldown";

    /**
     * set settings of a game
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                //fm set heads
                case HEADS -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_HEADS)){
                        if (cs instanceof Player player) {
                            if (GameManager.inst().getGame(args[2]) == null){
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                                return;
                            }

                            InventoryListener.inst().OpenInventory(args[2], player);
                        } else {
                            cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set gamelength <time in ticks, seconds or minutes>
                case GAME_LENGTH -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_GAMELENGTH)){
                        if (args.length >= 4){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                long ticks = 0;
                                for (int i = 3; i < args.length; i++){
                                    ticks += (new TimeHelper(args[i])).getTicks();
                                }

                                game.setGameTimeLength(ticks);
                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, GAME_LENGTH).replace(Lang.TYPE, String.valueOf(ticks))));
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set latejoin <bool>
                case LATE_JOIN_SHORT, LATE_JOIN_LONG -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_LATEJOIN)){
                        if (args.length >= 4){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                Boolean lateJoinAllowed = BooleanUtils.toBooleanObject(args[3]);
                                if (lateJoinAllowed != null){
                                    game.setAllowLateJoin(lateJoinAllowed);

                                    cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, LATE_JOIN_LONG).replace(Lang.TYPE, args[3])));
                                } else {
                                    cs.sendMessage(Lang.build(Lang.NO_BOOL.get().replace(Lang.VALUE, args[3])));
                                }
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set startpoint while standing at the right location
                case STARTPOINT_SHORT, STARTPOINT_LONG -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_LOCATIONS)){
                        if (cs instanceof LivingEntity livingEntity) {
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                Location loc = livingEntity.getLocation();
                                game.setStartLoc(loc);

                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, STARTPOINT_LONG).replace(Lang.TYPE, Lang.locationToString(loc))));
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set lobby while standing at the right location
                case LOBBY -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_LOCATIONS)){
                        if (cs instanceof LivingEntity livingEntity) {
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                Location loc = livingEntity.getLocation();
                                game.setLobbyLoc (loc);
                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, LOBBY).replace(Lang.TYPE, Lang.locationToString(loc))));
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set endpoint while standing at the right location
                case ENDPOINT_SHORT, ENDPOINT_LONG, ENDPOINT_ALT -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_LOCATIONS)){
                        if (cs instanceof LivingEntity livingEntity) {
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                Location loc = livingEntity.getLocation();
                                game.setQuitLoc(loc);
                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, ENDPOINT_LONG).replace(Lang.TYPE, Lang.locationToString(loc))));
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set atr <time in ticks>
                case AVERAGE_TICKS_UNTIL_REHEAD_LONG, AVERAGE_TICKS_UNTIL_REHEAD_SHORT -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_GAMELENGTH)){
                        if (args.length >= 4){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                long ticks = 0;
                                for (int i = 3; i < args.length; i++){
                                    ticks += (new TimeHelper(args[i])).getTicks();
                                }

                                game.setAverageTicksUntilRehead(ticks);
                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, AVERAGE_TICKS_UNTIL_REHEAD_LONG).replace(Lang.TYPE, String.valueOf(ticks))));
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set shp <percent double>
                //don't get fooled thou, we are generating just a random int, so every number after the point has no effect.
                case STARTING_PERCENT_LONG, STARTING_PERCENT_SHORT -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_GAMELENGTH)){
                        if (args.length >= 4){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                if (Utils.isDouble(args[3])){
                                    double percent = Double.parseDouble(args[3]);

                                    game.setStartingHiddenPercent(percent);
                                    cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, STARTING_PERCENT_LONG).replace(Lang.TYPE, String.valueOf(percent))));
                                } else {
                                    cs.sendMessage(Lang.build(Lang.NO_NUMBER.get().replace(Lang.VALUE, args[3])));
                                }
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                //fm set cooldown <game name> <time in ticks, seconds, minutes>
                case REHEAD_COOLDOWN_LONG, REHEAD_COOLDOWN_SHORT -> {
                    if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_GAMELENGTH)){
                        if (args.length >= 4){
                            Game game = GameManager.inst().getGame(args[2]);

                            if (game != null){
                                long millis = 0;
                                for (int i = 3; i < args.length; i++){
                                    //don't get confused, the time helper has a second field, but it is just the modulo of minutes of the whole time span
                                    millis += TimeUnit.SECONDS.toMillis ((new TimeHelper(args[i])).getTicks() / TimeHelper.TICKS_PER_SECOND);
                                }

                                game.setReheadCooldown(millis);
                                cs.sendMessage(Lang.build(Lang.SUCCESSFULLY_SET.get().replace(Lang.TYPE, REHEAD_COOLDOWN_LONG).replace(Lang.TYPE, String.valueOf(millis))));
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                            }
                        } else {
                            cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                        }
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
                    }
                }
                default -> cs.sendMessage(Lang.build(Lang.UNKNOWN_ARGUMENT.get().replace(Lang.VALUE, args[1])));
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        return null;
    }
}
