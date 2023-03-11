package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.PermissionUtils;
import de.greensurvivors.greenfinder.config.MainConfig;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FinderCommands implements CommandExecutor, TabCompleter {

	public static final String
			CMD                    = "greenfinder",

			/** **/
			CREATE_SHORT = "c",
			CREATE_LONG = "create",

			/** **/
			SET = "set",

			/** **/
			SHOW_SHORT = "s",
			SHOW_LONG = "show",

			/** **/
			REMOVE_SHORT = "rem",
			REMOVE_LONG = "remove",

			/** subcommand to Show plugin overview*/
			PLUGIN_SHORT           = "p",
			PLUGIN_LONG            = "plugin",

			/** subcommand for reloading this plugin*/
			RELOAD_SHORT = "rel",
			RELOAD_LONG = "reload",
			/** add a item*/
			ADD = "add",
			/** Show information about an item, used in WinterLectern and DropRecipe*/
			INFO = "info",
			/** list items of the same type*/
			LIST = "list",
			/** change period, used in DropRecipe and RestockRegions*/
			PERIOD = "period",
			/** remove an item, used in WinterLectern, DropRecipe, RestockRegion and piglin trade*/
			REMOVE = "remove",
			/** get the state of a specific value */
			GET = "get",
			/** change a button*/
			BUTTON = "button",
			/** prefixes for subcommands. used in Restock Regions and region Monsters*/
			WORLD_PREFIX = "-w";

	private static FinderCommands instance;

	public static FinderCommands inst() {
		if (instance == null)
			instance = new FinderCommands();
		return instance;
	}

	private FinderCommands() {}

	@Override
	public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args) {
		Bukkit.getScheduler().runTaskAsynchronously(GreenFinder.inst(), () -> handle(cs, args));
		return true;
	}

	/**
	 * Handle WinterWorker commands
	 * @param cs command sender
	 * @param args arguments
	 */
	private void handle(CommandSender cs, String[] args) {
		if (PermissionUtils.hasAnyPermission(cs)) {
			if (args.length > 0) {
				switch (args[0].toLowerCase()) {
					case SHOW_SHORT, SHOW_LONG -> ShowCmd.handleCmd(cs, args);
					case SET -> Set.handleCmd(cs, args);
					case CREATE_SHORT, CREATE_LONG -> CreateCmd.handleCmd(cs, args);
					case REMOVE_SHORT, REMOVE_LONG -> RemoveCmd.handleCmd(cs, args);
					case PLUGIN_SHORT, PLUGIN_LONG -> PluginCmd.handleCmd(cs);
					case RELOAD_SHORT, RELOAD_LONG -> reload(cs);
					default -> HelpCmd.handleCmd(cs);
				}
			} else {
				// help
				HelpCmd.handleCmd(cs);
			}
		} else {
			// help
			HelpCmd.handleCmd(cs);
		}
	}

	/**
	 * Handle reload command
	 * @param cs CommandSender
	 */
	private void reload(CommandSender cs) {
		if (PermissionUtils.hasPermission(cs, PermissionUtils.WINTER_ADMIN, PermissionUtils.WINTER_RELOAD)) {
			MainConfig.inst().reloadMain();
			cs.sendMessage(Lang.build(Lang.RELOAD.get()));
		} else {
			cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
		}
	}

	//todo cowntdown; teleport into game and out
	@Override //set time a game lasts; until a stand can respawn; spawnpunkt; endpunkt; Lobbypunkt;
	//get what game a stand belongs to
	//join
	public List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args) {
			if (args.length == 1) { //todo permissions
				return Stream.of(SHOW_SHORT, SHOW_LONG,
						CREATE_SHORT, CREATE_LONG,
						REMOVE_SHORT, REMOVE_LONG,
						SET,
						RELOAD_SHORT, RELOAD_LONG).filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
			} else {
				switch (args[0]){
					case CREATE_SHORT, CREATE_LONG -> {
						return CreateCmd.handleTap(cs, args);
					}
					case REMOVE_SHORT, REMOVE_LONG -> {
						return RemoveCmd.handleTap(cs, args);
					}
				}
			}

		return Collections.emptyList();
	}
}
