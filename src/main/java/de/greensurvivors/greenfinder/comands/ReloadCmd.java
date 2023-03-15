package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.PermissionUtils;
import de.greensurvivors.greenfinder.config.MainConfig;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.command.CommandSender;

public class ReloadCmd {

    /**
     * Handle reload command
     * @param cs CommandSender
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.WINTER_ADMIN, PermissionUtils.WINTER_RELOAD)) {
            MainConfig.inst().reloadMain();
            cs.sendMessage(Lang.build(Lang.RELOAD.get()));
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }
}
