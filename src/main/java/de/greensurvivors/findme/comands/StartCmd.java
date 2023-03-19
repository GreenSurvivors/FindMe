package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;

import java.util.List;

public class StartCmd {
    /**
     * start a game
     * /fm start <game name>
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_START)){
            if (args.length >= 2){
                Game game = GameManager.inst().getGame(args[1]);

                if (game != null){
                    if (game.getGameState() == Game.GameStates.ENDED){
                        //start the game
                       game.startup();

                        cs.sendMessage(Lang.build(Lang.STARTING_GAME.get().replace(Lang.VALUE, args[1])));
                    } else {
                        cs.sendMessage(Lang.build(Lang.GAME_ALREADY_ACTIVE.get()));
                    }
                } else {
                    //no game by this name exits
                    cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[1])));
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
            }
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        //todo
        return null;
    }
}
