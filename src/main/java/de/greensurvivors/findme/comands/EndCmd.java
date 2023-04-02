package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class EndCmd {
    /**
     * end a game
     * /fm end <name>
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_END)){
            if (args.length >= 2){
                Game game = GameManager.inst().getGame(args[1]);

                if (game != null){
                    //end the game
                    //this does not check for game state so a game can always get forced to end.
                    game.end();

                    cs.sendMessage(Lang.build(Lang.ENDING_GAME.get().replace(Lang.VALUE, args[1])));
                } else {
                    //no game by this name exits
                    cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[1])));
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
            }
        }
    }

    public static List<String> handleTab(CommandSender cs, String[] args) {
        if (args.length == 2 && PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_END)){
            return GameManager.inst().getGameNames().stream().filter(s -> s.toLowerCase().startsWith(args[1])).collect(Collectors.toList());
        }
        return List.of();
    }
}
