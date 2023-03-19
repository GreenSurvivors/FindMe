package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;

public class ListCmd { //list all games

    /**
     * list all games
     * /fm list
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) { //todo
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_LIST)){
            cs.sendMessage("list");
            for (String name : GameManager.inst().getGameNames()){
                Game game = GameManager.inst().getGame(name);

                cs.sendMessage(" - " + game.getName());
                cs.sendMessage("    game state: " + game.getGameState().getName());
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }
}
