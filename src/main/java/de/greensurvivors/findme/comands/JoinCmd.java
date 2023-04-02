package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class JoinCmd {
    /**
     * join a game
     * /fm join <game name>
     * /fm join <game name> <player> to force somebody to join a game
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_OTHER_PLAYERS)){
                Player otherPlayer = Bukkit.getPlayer(args[2]);

                if (otherPlayer != null){
                    Game gameOfPlayer = GameManager.inst().getGameOfPlayer(otherPlayer);
                    if (gameOfPlayer != null){
                        cs.sendMessage(Lang.build(Lang.ALREADY_IN_GAME_OTHER.get().replace(Lang.VALUE, otherPlayer.getName()).replace(Lang.TYPE, gameOfPlayer.getName())));
                    } else {
                        Game gameToJoin = GameManager.inst().getGame(args[1]);

                        if (gameToJoin != null){
                            GameManager.inst().playerJoinGame(otherPlayer, gameToJoin);
                        } else {
                            cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[1])));
                        }
                    }
                } else {
                    cs.sendMessage(Lang.build(Lang.PLAYER_NOT_ONLINE.get().replace(Lang.VALUE, args[2])));
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
            }
        } else if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
            if (args.length == 2){
                if (cs instanceof Player player) {
                        Game gameOfPlayer = GameManager.inst().getGameOfPlayer(player);

                        if (gameOfPlayer != null){
                            cs.sendMessage(Lang.build(Lang.ALREADY_IN_GAME_SELF.get().replace(Lang.VALUE, gameOfPlayer.getName())));
                        } else {
                            Game gameToJoin = GameManager.inst().getGame(args[1]);

                            if (gameToJoin != null){
                                GameManager.inst().playerJoinGame(player, gameToJoin);
                            } else {
                                cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[1])));
                            }
                        }
                } else {
                    cs.sendMessage(Lang.NO_PLAYER.get());
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }
// fm join <game name>
// fm join <game name> <player> to force somebody to join a game
    public static List<String> handleTab(CommandSender cs, String[] args) {
        if (args.length == 2) {
            if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
                return GameManager.inst().getGameNames().stream().filter(s -> s.toLowerCase().startsWith(args[1])).collect(Collectors.toList());
            }
        } else if (args.length == 3){
            if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_OTHER_PLAYERS)){
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.toLowerCase().startsWith(args[2])).collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
