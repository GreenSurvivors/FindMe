package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.FindMe;
import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.language.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PluginCmd {

	/**
	 * Handle plugin command
	 * /fm plugin
	 * @param cs CommandSender
	 */
	public static void handleCmd(CommandSender cs) {
		if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLUGIN)) {
			// collect all messages to send at once
			List<Component> components = new ArrayList<>();
			// header
			components.add(Lang.build(Lang.PLUGIN_HEADER.get()));
			// version (update ?)
			components.add(Lang.build(Lang.PLUGIN_VERSION.get().replace(Lang.VALUE, FindMe.inst().getDescription().getVersion())));

			// send components
			cs.sendMessage(Lang.join(components));
		} else {
			cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
		}
	}
}
