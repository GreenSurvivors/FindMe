package de.greensurvivors.greenfinder.comands;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.dataObjects.Game;
import de.greensurvivors.greenfinder.dataObjects.GameManager;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class CreateCmd {
    private static final String
            GAME = "game",
            STAND = "stand",
            SIGN = "sign";

    public static void handleCmd(CommandSender cs, String[] args) {
        if (args.length >= 3){
            switch (args[1]){
                //gf c game <name>
                case GAME -> {
                    if (GameManager.inst().addGame(args[2])){
                        //success
                        cs.sendMessage(Lang.build("success"));
                    } else {
                        //unsuccessful
                        cs.sendMessage(Lang.build("game already exits"));
                    }
                }
                //gf c stand <name>
                case STAND -> {
                    if (cs instanceof LivingEntity livingEntity){
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game != null){
                            //summon the entity in sync
                            Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
                            game.addHiddenStand(livingEntity.getLocation());
                            });

                            cs.sendMessage(Lang.build("created new stand."));
                        } else {
                            //no game by this name exits
                            cs.sendMessage(Lang.build("Unknown game"));
                        }
                    } else {
                        //no location of cs
                        cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                    }
                }
                case SIGN -> {
                    if (cs instanceof LivingEntity livingEntity){
                        Game game = GameManager.inst().getGame(args[2]);

                        if (game != null){
                            //set the sign text in sync
                            Bukkit.getScheduler().runTask(GreenFinder.inst(), () -> {
                                Sign sign = getSignLookingAt(livingEntity);

                                if (sign != null){
                                    sign.line(1, Lang.build("[join]"));
                                    sign.line(2, Lang.build(game.getName()));

                                } else {
                                    cs.sendMessage(Lang.build("Lang.NO_SIGN.get()"));
                                }
                            });

                            cs.sendMessage(Lang.build("created new sign."));
                        } else {
                            //no game by this name exits
                            cs.sendMessage(Lang.build("Unknown game"));
                        }
                    } else {
                        //no location of cs
                        cs.sendMessage(Lang.build(Lang.NO_PLAYER.get()));
                    }
                }
                default -> {
                    //didn't understand what should get created
                    cs.sendMessage(Lang.build("try /help"));
                }
            }
        }
    }

    public static List<String> handleTap(CommandSender cs, String[] args) {
        switch (args.length){
            case 1 -> {
                return List.of(GAME, STAND, SIGN);
            }
            case 2 -> {
                if (args[1].equalsIgnoreCase(STAND) || args[1].equalsIgnoreCase(SIGN)){
                    return new ArrayList<>(GameManager.inst().getGameNames());
                }
            }
        }
        return null;
    }
    /**
     * Get location of a sign a living entity is looking at.
     * @param entity looking at a sign
     * @return location of sign, or null if the entity is not looking at a sign
     */
    private static Sign getSignLookingAt(LivingEntity entity) {
        // player is looking at a sign
        Block block = entity.getTargetBlockExact(5);

        if (block != null && Tag.ALL_SIGNS.isTagged(block.getType()))  {
            return (Sign)block.getState();
        } else {
            return null;
        }
    }
}
