package de.greensurvivors.greenfinder;

import org.bukkit.command.CommandSender;

public enum PermissionUtils {
	FINDER_REMOVE ("greenfinder.remove"),
	FINDER_REMOVE_GAME ("greenfinder.remove.game"),
	FINDER_REMOVE_STAND ("greenfinder.remove.stand"),

	FINDER_CREATE ("greenfinder.create"),
	FINDER_CREATE_GAME ("greenfinder.create.game"),
	FINDER_CREATE_STAND ("greenfinder.create.stand"),
	FINDER_CREATE_SIGN ("greenfinder.create.sign"),

	FINDER_SET("greenfinder.set"),
	FINDER_SET_HEADS("greenfinder.set.heads"),
	FINDER_SET_GAMELENGTH("greenfinder.set.gamelength"),
	FINDER_SET_LATEJOIN("greenfinder.set.latejoin"),
	FINDER_SET_LOCATIONS("greenfinder.set.locations"),

	FINDER_START("greenfinder.start"),
	FINDER_END("greenfinder.end"),

	FINDER_SHOW("greenfinder.show"),

	FINDER_LIST ("greenfinder.list"),

	FINDER_ADMIN("greenfinder.admin"),
	FINDER_PLAYER("greenfinder.player"),

	FINDER_PLUGIN("greenfinder.plugin"),
	FINDER_RELOAD("greenfinder.reload");

	private final String value;

	PermissionUtils(String value) {
		this.value = value;
	}

	public String get() {
		return value;
	}

	/**
	 * Check if CommandSender has any of the given permissions.
	 * @param cs CommandSender to check for
	 * @param permission permissions to check
	 * @return true if cs has at least one of the given permissions.
	 */
	public static boolean hasPermission(CommandSender cs, PermissionUtils...permission) {
		for (PermissionUtils p : permission) {
			if (cs.hasPermission(p.get()))
				return true;
		}

		return false;
	}

	/**
	 * Check if CommandSender has any permission from this plugin.
	 * @param cs CommandSender to check for
	 * @return true if cs has at least one permission from this plugin.
	 */
	public static boolean hasAnyPermission(CommandSender cs) {
		for (PermissionUtils p : values()) {
			if (cs.hasPermission(p.get()))
				return true;
		}
		return false;
	}
}
