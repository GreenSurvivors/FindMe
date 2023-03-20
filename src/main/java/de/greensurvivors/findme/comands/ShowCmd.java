package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.Utils;
import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShowCmd {
    private final static int DEFAULT_RANGE = 20;

    /**
     * show all hideaways of a given game in a range
     * /fm show <game name>
     * /fm show <game> <range>
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SHOW)){
            if (cs instanceof LivingEntity livingEntity){
                if (args.length >= 2){
                    Game game = GameManager.inst().getGame(args[1]);

                    if (game != null){
                        int range = DEFAULT_RANGE;
                        if (args.length >= 3 && Utils.isInt(args[2])){
                            range = Integer.parseInt(args[2]);
                        }

                        //get the entity glowing
                        game.showAroundLocation(livingEntity.getLocation(), range);
                    } else {
                        cs.sendMessage(Lang.build(Lang.UNKNOWN_GAME.get().replace(Lang.VALUE, args[1])));
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

// fm show <game name>
// fm show <game> <range>
    public static List<String> handleTap(CommandSender cs, String[] args) {
        if ( PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SHOW)){
            if (args.length == 2) {
                return GameManager.inst().getGameNames().stream().filter(s -> s.toLowerCase().startsWith(args[1])).collect(Collectors.toList());

            } else if (args.length == 3){
                return Stream.of(String.valueOf(DEFAULT_RANGE)).filter(s -> s.toLowerCase().startsWith(args[2])).collect(Collectors.toList());
            }
        }

        return List.of();
    }
}
