package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.PermissionUtils;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.command.CommandSender;

import static de.greensurvivors.greenfinder.PermissionUtils.hasAnyPermission;
import static de.greensurvivors.greenfinder.PermissionUtils.hasPermission;

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
			if (hasPermission(cs, PermissionUtils.FINDER_ADMIN))
				cs.sendMessage(Lang.build(Lang.HELP_WIKI.get(), null, null, null, "https://bookstack.greensurvivors.de/books/plugins/page/paper-winterworker"));
		} else {
			cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
		}
	}
}
