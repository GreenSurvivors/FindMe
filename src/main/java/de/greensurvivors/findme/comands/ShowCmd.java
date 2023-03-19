package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.Utils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class ShowCmd {
    /**
     * show all hidden stands of a given game in a range
     * /fm show <game name>
     * /fm show <game> <range>
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SHOW)){
            if (cs instanceof LivingEntity livingEntity){
                if (args.length >= 3){
                    Game game = GameManager.inst().getGame(args[2]);

                    if (game != null){
                        int range = 20;
                        if (args.length >= 4 && Utils.isInt(args[3])){
                            range = Integer.parseInt(args[3]);
                        }

                        //get the entity glowing
                        game.showAroundLocation(livingEntity.getLocation(), range);
                    } else {
                        cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[2])));
                    }
                } else {
                    cs.sendMessage(Lang.build(Lang.NOT_ENOUGH_ARGS.get()));
                }
            } else {
                cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        //todo
        return null;
    }
}
