package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FindMeCommands implements CommandExecutor, TabCompleter {

	public static final String
			CMD                    = "findme",

			/** subcommand for creating a new game, hidden armor stands and signs **/
			CREATE_SHORT = "c",
			CREATE_LONG = "create",

			/** subcommand to list all games **/
			LIST = "list",

			/** subcommand for setting the config values of a game **/
			SET = "set",

			/** subcommand to show all (nearby) hidden armor stands of a game**/
			SHOW = "show",

			/** subcommand to start a game **/
			START = "start",

			/** subcommand for aboarding a game **/
			END = "end",

			/** Show information about a game / hidden armor stande*/
			INFO = "info",

			/** subcommand to join a game**/
			JOIN = "join",

			/** subcommand to delete a game or a hidden stand. signs can just get broken**/
			REMOVE_SHORT = "rem",
			REMOVE_LONG = "remove",

			/** subcommand to quit a game**/
			QUIT = "quit",

			/** subcommand to Show plugin overview*/
			PLUGIN_SHORT           = "p",
			PLUGIN_LONG            = "plugin",

			/** subcommand for reloading this plugin*/
			RELOAD_SHORT = "rel",
			RELOAD_LONG = "reload";

	private static FindMeCommands instance;

	public static FindMeCommands inst() {
		if (instance == null)
			instance = new FindMeCommands();
		return instance;
	}

	private FindMeCommands() {}

	@Override
	public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args) {
		handle(cs, args);
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
					case SHOW -> ShowCmd.handleCmd(cs, args);
					case SET -> SetCmd.handleCmd(cs, args);
					case CREATE_SHORT, CREATE_LONG -> CreateCmd.handleCmd(cs, args);
					case LIST -> ListCmd.handleCmd(cs, args);
					case START -> StartCmd.handleCmd(cs, args);
					case END -> EndCmd.handleCmd(cs, args);
					case JOIN -> JoinCmd.handleCmd(cs, args);
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

	@Override
	//get what game a stand belongs to
	//todo quit sign
	//info (about) a game -> Lobby/Start/quit pos; state; etc
	//automode -> min/maxplayers; waiting time for players to join
	public List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args) {
			if (args.length == 1) {
				List<String> result = new ArrayList<>();

				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SHOW)){
					result.add(SHOW);
				}
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_CREATE, PermissionUtils.FINDME_CREATE_STAND, PermissionUtils.FINDME_CREATE_SIGN, PermissionUtils.FINDME_CREATE_GAME)){
					result.add(CREATE_SHORT);
					result.add(CREATE_LONG);
				}
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_LIST)){
					result.add(LIST);
				}
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_START)){
					result.add(START);
				}
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_END)){
					result.add(END);
				}
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_REMOVE, PermissionUtils.FINDME_REMOVE_STAND, PermissionUtils.FINDME_REMOVE_GAME)){
					result.add(REMOVE_LONG);
					result.add(REMOVE_SHORT);
				}
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_SET, PermissionUtils.FINDME_SET_GAMELENGTH, PermissionUtils.FINDME_SET_HEADS, PermissionUtils.FINDME_SET_LATEJOIN, PermissionUtils.FINDME_SET_LOCATIONS)){
					result.add(SET);
				}
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_PLAYER)){
					result.add(JOIN);
				}
				//this has no permission check, so a player can always quit.
				result.add(QUIT);
				if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_RELOAD)){
					result.add(RELOAD_LONG);
					result.add(RELOAD_SHORT);
				}

				return result.stream().filter(s -> args[0].toLowerCase().startsWith(s)).collect(Collectors.toList());
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
