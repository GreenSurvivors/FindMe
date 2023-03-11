package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.dataObjects.GameManager;
import org.bukkit.command.CommandSender;

public class ListCmd { //list all games

    public static void handleCmd(CommandSender cs, String[] args) { //todo
        for (String name :GameManager.inst().getGameNames()){
            switch (GameManager.inst().getGame(name).getGameState()){
                case ACTIVE -> {}
                case STARTING -> {}
                case ENDED -> {}
                default -> {//how?
                }
            }
        }


        cs.sendMessage("list");
    }
}
