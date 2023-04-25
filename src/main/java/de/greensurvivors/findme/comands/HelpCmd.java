package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;

public class HelpCmd {
	/**
	 * Handle help command
	 * @param cs CommandSender
	 */
	public static void handleCmd(CommandSender cs) {
		if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_HELP)) {
			cs.sendMessage(Lang.build(Lang.HELP_HEADER.get()));
			// wiki
			if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN))
				cs.sendMessage(Lang.build(Lang.HELP_WIKI.get(), null, null, null, "https://bookstack.greensurvivors.de/books/plugins/page/paper-findme"));
		} else {
			cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
		}
	}
}
