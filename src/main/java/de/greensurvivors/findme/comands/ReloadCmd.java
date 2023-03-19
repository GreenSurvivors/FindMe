package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.config.MainConfig;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;

public class ReloadCmd {

    /**
     * Handle reload command
     * /fm reload
     * @param cs CommandSender
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_RELOAD)) {
            MainConfig.inst().reloadAll();
            cs.sendMessage(Lang.build(Lang.RELOAD.get()));
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }
}
