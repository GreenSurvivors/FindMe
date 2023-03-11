package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.listener.InventoryListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Set {
    private static final String
            HEADS = "heads",
            SIGN = "sign",
            GAME_LENGTH = "length";
    public static void handleCmd(CommandSender cs, String[] args) {
        if (cs instanceof Player player) {
            if (GameManager.inst().getGame(args[2]) == null){
                cs.sendMessage("no game");
                return;
            }

            InventoryListener.inst().OpenInventory(args[2], player);
        } else {
            cs.sendMessage("no player");
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        return null;
    }
}
