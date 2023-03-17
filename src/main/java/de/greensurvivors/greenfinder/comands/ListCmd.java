package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.PermissionUtils;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.command.CommandSender;

public class ListCmd { //list all games

    public static void handleCmd(CommandSender cs, String[] args) { //todo
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDER_ADMIN, PermissionUtils.FINDER_LIST)){
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
