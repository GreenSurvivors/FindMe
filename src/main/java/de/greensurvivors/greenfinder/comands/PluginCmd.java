package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.PermissionUtils;
import de.greensurvivors.greenfinder.language.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginCmd {

	/**
	 * Handle plugin command
	 * @param cs CommandSender
	 */
	public static void handleCmd(CommandSender cs) {
		if (PermissionUtils.hasPermission(cs, PermissionUtils.WINTER_ADMIN, PermissionUtils.WINTER_PLUGIN)) {
			// collect all messages to send at once
			List<Component> components = new ArrayList<>();
			// header
			components.add(Lang.build(Lang.PLUGIN_HEADER.get()));
			// version (update ?)
			components.add(Lang.build(Lang.PLUGIN_VERSION.get().replace(Lang.VALUE, GreenFinder.inst().getDescription().getVersion())));

			// send components
			cs.sendMessage(Lang.join(components));
		} else {
			cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
		}
	}

	//currently no additional parameters are supported.
	public static List<String> handleTap(CommandSender cs, String[] args) {
		return Collections.emptyList();
	}
}
