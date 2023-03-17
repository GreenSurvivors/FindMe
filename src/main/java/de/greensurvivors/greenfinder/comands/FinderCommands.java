package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.PermissionUtils;
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
			LIST = "list",

			/** **/
			SET = "set",

			/** **/
			SHOW_LONG = "show",

			/** **/
			START = "start",

			/** **/
			REMOVE_SHORT = "rem",
			REMOVE_LONG = "remove",

			/** **/
			QUIT = "quit",

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
			/** change period, used in DropRecipe and RestockRegions*/
			PERIOD = "period",
			/** remove an item, used in WinterLectern, DropRecipe, RestockRegion and piglin trade*/
			REMOVE = "remove",
			/** get the state of a specific value */
			GET = "get",
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
					case SHOW_LONG -> ShowCmd.handleCmd(cs, args);
					case SET -> SetCmd.handleCmd(cs, args);
					case CREATE_SHORT, CREATE_LONG -> CreateCmd.handleCmd(cs, args);
					case LIST -> ListCmd.handleCmd(cs, args);
					case START -> StartCmd.handleCmd(cs, args);
					case QUIT -> QuitCmd.handleCmd(cs, args);
					case REMOVE_SHORT, REMOVE_LONG -> RemoveCmd.handleCmd(cs, args);
					case PLUGIN_SHORT, PLUGIN_LONG -> PluginCmd.handleCmd(cs);
					case RELOAD_SHORT, RELOAD_LONG -> ReloadCmd.handleCmd(cs, args);
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

	@Override //set time/chance until a stand can respawn;
	//get what game a stand belongs to
	//info (about) a game -> Lobby/Start/quit pos
	//automode -> min/maxplayers; waiting time for players to join
	//filter tap suggestions after what was already typed in
	//todo translations
	//join cmd
	//game force end cmd
	//language
	public List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args) {
			if (args.length == 1) { //todo permissions
				return Stream.of(SHOW_LONG,
						CREATE_SHORT, CREATE_LONG,
						LIST,
						START,
						REMOVE_SHORT, REMOVE_LONG,
						SET,
						QUIT,
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
