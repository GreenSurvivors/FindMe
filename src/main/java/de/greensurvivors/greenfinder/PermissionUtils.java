package de.greensurvivors.greenfinder;

import org.bukkit.command.CommandSender;

public enum PermissionUtils {

	WINTER_ADMIN	("winterworker.admin"),

	WINTER_PLUGIN	("winterworker.plugin"),
	WINTER_RELOAD	("winterworker.reload");

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
