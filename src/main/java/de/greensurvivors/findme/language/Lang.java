package de.greensurvivors.findme.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public enum Lang implements Cons {
	SUCCESSFULLY_SET(String.format("&2%s was successfully set to &6'&e%s&6'.", TYPE, VALUE)),
	SUCCESSFULLY_REMOVED(String.format("&2%s was successfully removed.", VALUE)),
	SUCCESSFULLY_CREATED(String.format("&2Successfully created new %s", VALUE)),
	STARTING_GAME(String.format("&2Starting game %s.", VALUE)),
	ENDING_GAME(String.format("&2Ending game %s.", VALUE)),

	SIGN_JOIN("&2[join fm]"),

	GAME_ALREADY_ACTIVE("The game is already active."),
	GAME_ALREADY_EXISTS(String.format("&cThe game &6'&e%s&6'&c already exists.", VALUE)),

	ALREADY_IN_GAME_SELF(String.format("&cYou are already in game &6'%s&6'&c. Use &e/fm quit", VALUE)),
	ALREADY_IN_GAME_OTHER(String.format("&6'&e%s&6'&c is already in game &6'%s&6'&c. Use &e/fm quit", VALUE, TYPE)),

	MESSAGE_JOIN("&2Welcome in a game of 'find me!'. use &e/fm quit&2 to exit the game."),
	MESSAGE_OBJECTIVE("&2Objective: find as many hidden things as fast as possible!"),

	QUIT_OTHER(String.format("&2Player &6'%s&6'&8 quit the game", VALUE)),
	QUIT_SELF("&aSuccessfully quit the game."),

	PLAYER_NOT_ONLINE(String.format("&6'&e%s&6'&c is not a player or not online.", VALUE)),
	PLAYER_NOT_INGAME(String.format("&cPlayer &6'&e%s&6'&c is in a game.", VALUE)),

	NO_PLAYER("&cYou have to be a player."),
	NO_NUMBER(String.format("&6'&e%s&6' &cis not a valid number.", VALUE)),
	NO_NEARBY_STAND("&cNo nearby stands where found. Try &e/fm list&c."),
	NOT_IN_GAME("&cYou are not in a game."),
	NO_BOOL(String.format("&6'&e%s&6' &cis not a valid boolean.", VALUE)),
	NO_PERMISSION_COMMAND("&cYou have no permission to perform this command."),
	NO_PERMISSION_SOMETHING("&cYou have no permission to do that."),
	NO_SUCH_PLAYER(String.format("&cCould not get a valid player from %s", VALUE)),
	NO_SIGN("&cYou are not looking at a sign."),
	UNKNOWN_ARGUMENT(String.format("&cUnknown or wrong argument &6'&e%s&6'&c. Try &e/fm help", VALUE)),
	UNKNOWN_ERROR("&cUnknown Error. What happened?"),
	UNKNOWN_GAME(String.format("&cUnknown Game &6'&e%s&6'. Try &e/fm list", VALUE)),
	NOT_ENOUGH_ARGS("&cNot enough arguments."),

	FORMAT_LOCATION(String.format("&f%s&6, &f%s&e, &f%s&e, &f%s", WORLD, X, Y, Z)),

	HELP_HEADER			("&a-<(&FindMe &e- &6Help&a)>-"),
	HELP_WIKI("&7See more detailed information on the wiki page."),

	PLUGIN_HEADER("&a-<(&6GreenTreasure&a)>-"),
	PLUGIN_VERSION(String.format("&aVersion&6: &e%s", VALUE)),

	RELOAD("&aReloaded.");


	private String value;

	Lang(String value) {
		this.value = value;
	}

	/**
	 * uncolored
	 *
	 * @return value
	 */
	public String get() {
		return value;
	}

	/**
	 * Set value.
	 *
	 * @param value to set
	 */
	public void set(String value) {
		this.value = value;
	}


	/**
	 * Builds a Component from given text.
	 *
	 * @param args to build from
	 * @return Component
	 */
	public static Component build(String args) {
		return build(args, null, null, null, null);
	}

	/**
	 * Build a Component from given text with properties.
	 * There can only be one click action.
	 *
	 * @param text       to build from
	 * @param command    to execute on click
	 * @param hover      to Show when mouse hovers over the text
	 * @param suggestion to Show on click
	 * @return Component
	 */
	public static Component build(String text, String command, Object hover, String suggestion) {
		return build(text, command, hover, suggestion, null);
	}

	/**
	 * Build a Component from given text with properties.
	 * There can only be one click action.
	 *
	 * @param text       to build from
	 * @param command    to execute on click
	 * @param hover      to Show when mouse hovers over the text
	 * @param suggestion to Show on click
	 * @param link       to open on click
	 * @return Component
	 */
	public static Component build(String text, String command, Object hover, String suggestion, String link) {
		if (text != null) {
			TextComponent tc = rgb(Component.text(text));
			// command suggestion
			if (suggestion != null && !suggestion.isBlank()) {
				tc = tc.clickEvent(ClickEvent.suggestCommand(suggestion));
			}
			// run command
			if (command != null && !command.isBlank()) {
				tc = tc.clickEvent(ClickEvent.runCommand(command));
			}
			// open link
			if (link != null && !link.isBlank()) {
				tc = tc.clickEvent(ClickEvent.openUrl(link));
			}
			// hover effect
			if (hover != null) {
				if (hover instanceof Component component)
					tc = tc.hoverEvent(component);
				if (hover instanceof ItemStack item)
					tc = tc.hoverEvent(item.asHoverEvent());
				if (hover instanceof String s)
					tc = tc.hoverEvent(Component.text(s));
			}
			return tc;
		}
		return null;
	}

	/**
	 * Join Components together with new lines as separator.
	 *
	 * @param components to join together
	 * @return Component
	 */
	public static Component join(Iterable<? extends Component> components) {
		return Component.join(JoinConfiguration.separator(Component.newline()), components);
	}

	/**
	 * Get a Component colored with rgb colors.
	 *
	 * @param tc to convert colors
	 * @return Component with all color codes converted
	 */
	private static TextComponent rgb(TextComponent tc) {
		String text = tc.content();
		if (text.contains("&#")) {
			// find first hexColor
			int i = text.indexOf("&#");
			// substring first part (old color)
			tc = LegacyComponentSerializer.legacyAmpersand().deserialize(text.substring(0, i));
			//alternative: tc = LegacyComponentSerializer.legacy('&').deserialize(text.substring(0, i));

			//explicitly deny unset decorations. If not done the server trys to set them after reload
			//and items with this component as lore can't be stacked.
			for (TextDecoration t : TextDecoration.values()) {
				if (tc.decorations().containsKey(t))
					tc = tc.decoration(t, false);
			}

			// substring last part (new color)
			tc = tc.append(rgb(Component.text(text.substring(i + 8), TextColor.fromHexString(text.substring(i + 1, i + 8)))));
		} else {
			// text with legacy ChatColor
			tc = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
		}
		return tc;
	}

	/**
	 * formats a location to a string
	 *
	 * @param loc location to format
	 * @return the formatted string or "-" if the locaion was null
	 */
	public static String locationToString(Location loc) {
		if (loc != null) {
			return FORMAT_LOCATION.get()
					.replace(Lang.WORLD, loc.getWorld().getName())
					.replace(Lang.X, Integer.toString(loc.getBlockX()))
					.replace(Lang.Y, Integer.toString(loc.getBlockY()))
					.replace(Lang.Z, Integer.toString(loc.getBlockZ()));
		}
		return "-";
	}
}
