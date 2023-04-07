package de.greensurvivors.findme.comands;

import de.greensurvivors.findme.GreenLogger;
import de.greensurvivors.findme.PermissionUtils;
import de.greensurvivors.findme.Utils;
import de.greensurvivors.findme.dataObjects.GameManager;
import de.greensurvivors.findme.language.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ListCmd { //list all games
    private static final byte GAMES_PER_PAGE = 10;

    /**
     * list all games
     * /fm list [page - optional]
     * @param cs
     * @param args
     */
    public static void handleCmd(CommandSender cs, String[] args) {
        if (PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_LIST)){
            ArrayList<String> gameNames = new ArrayList<>(GameManager.inst().getGameNames());
            final int numOfGames = gameNames.size();

            if (numOfGames > 0){
                final ArrayList<Component> listResult = new ArrayList<>();
                final int numPages = (int) Math.ceil((double)numOfGames / (double)GAMES_PER_PAGE);

                final int pageNow; //please note: we are start counting with page 1, not 0 for convenience of users of this plugin
                if (args.length >= 2){
                    if (Utils.isInt(args[1])){
                        //limit page to how many exits
                        pageNow = Math.max(1, Math.min(numPages, Integer.parseInt(args[1])));

                        GreenLogger.log(Level.INFO, String.valueOf(pageNow));
                    } else {
                        cs.sendMessage(Lang.build(Lang.NO_NUMBER.get().replace(Lang.VALUE, args[1])));
                        return;
                    }
                } else {
                    pageNow = 1;
                }

                final int MAX_BOOKS_THIS_PAGE = Math.min(numOfGames, pageNow * GAMES_PER_PAGE);

                listResult.add(Lang.build(Lang.LIST_HEADER.get().replace(Lang.VALUE, String.valueOf(pageNow)).replace(Lang.VALUE2, String.valueOf(numPages))));

                //add the books for the page
                for (int num = (pageNow -1) * GAMES_PER_PAGE; num < MAX_BOOKS_THIS_PAGE; num++){
                    String gameName = gameNames.get(num);

                    listResult.add(
                            Lang.build(Lang.LIST_ENTRY.get().
                                    replace(Lang.TYPE, gameName).
                                    replace(Lang.VALUE, GameManager.inst().getGame(gameName).getGameState().getName())));
                }

                cs.sendMessage(Lang.join(listResult));
            } else {
                cs.sendMessage(Lang.build(Lang.LIST_EMPTY.get()));
            }
        } else {
            cs.sendMessage(Lang.build(Lang.NO_PERMISSION_COMMAND.get()));
        }
    }

    public static List<String> handleTab(CommandSender cs, String[] args) {
        if (args.length == 2 && PermissionUtils.hasPermission(cs, PermissionUtils.FINDME_ADMIN, PermissionUtils.FINDME_LIST)){
            ArrayList<String> result = new ArrayList<>();

            //cache number of pages to not recalculate every loop
            final int PAGES = (int) Math.ceil((double)GameManager.inst().getGameNames().size() / (double)GAMES_PER_PAGE);

            //make list of all known pages
            for (int page = 1; page <= PAGES; page++){
                result.add(String.valueOf(page));
            }

            //filter by already given argument
            return result.stream().filter(s -> s.startsWith(args[1])).toList();
        }

        //only add suggestions for first argument after list sub command
        return List.of();
    }
}
