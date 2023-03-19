package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.command.CommandSender;

import static de.greensurvivors.findme.PermissionUtils.hasAnyPermission;
import static de.greensurvivors.findme.PermissionUtils.hasPermission;

public class HelpCmd {
	//todo make help where the commands are explained

	/**
	 * Handle help command
	 * @param cs CommandSender
	 */
	public static void handleCmd(CommandSender cs) {
		if (hasAnyPermission(cs)) {
			cs.sendMessage(Lang.build(Lang.HELP_HEADER.get()));
			// wiki
			if (hasPermission(cs, PermissionUtils.FINDME_ADMIN))
				cs.sendMessage(Lang.build(Lang.HELP_WIKI.get(), null, null, null, "https://bookstack.greensurvivors.de/books/plugins/page/paper-findme"));
		} else {
			cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
		}
	}
}
