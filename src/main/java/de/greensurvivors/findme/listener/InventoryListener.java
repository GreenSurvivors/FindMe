package de.greensurvivors.findme.listener;

import de.greensurvivors.findme.dataObjects.Game;
import de.greensurvivors.findme.dataObjects.GameManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;

public class InventoryListener  implements Listener {
    private static InventoryListener instance;

    public static InventoryListener inst() {
        if (instance == null) {
            instance = new InventoryListener();
        }
        return instance;
    }
    private final HashMap<String, Inventory> cachedViews = new HashMap<>();
    private final HashMap<String, Integer> numOfOpedInventories = new HashMap<>();

    private @Nullable Inventory buildHeadInv(String gameName){
        Game game = GameManager.inst().getGame(gameName);
        if (game == null)
            return null;

        final int INVENTORY_SIZE = 6 * 9;

        //inventory holder has to be null, or non op player will not be able to open the inventory
        Inventory inventory = Bukkit.getServer().createInventory(null, INVENTORY_SIZE, PlainTextComponentSerializer.plainText().deserialize(game.getName()).color(NamedTextColor.GOLD));
        inventory.setMaxStackSize(1024);
        LinkedHashSet<ItemStack> heads = game.getHeads();

        //add items
        for (ItemStack itemStack : heads){
            inventory.addItem(itemStack);
        }

        return inventory;
    }

    public void OpenInventory(String gameName, Player player){
        Inventory inventory = cachedViews.get(gameName);

        if (inventory != null){
            player.openInventory(inventory);
            numOfOpedInventories.put(gameName, numOfOpedInventories.get(gameName)+1);
        } else {
            inventory = buildHeadInv(gameName);
            if (inventory != null){
                cachedViews.put(gameName, inventory);
                player.openInventory(inventory);
                numOfOpedInventories.put(gameName, 1);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        for(String name : GameManager.inst().getGameNames()){
            if(PlainTextComponentSerializer.plainText().serialize(event.getView().title()).equalsIgnoreCase(name)){
                Game game = GameManager.inst().getGame(name);

                if (game != null){
                    game.setHeads(Arrays.stream(event.getView().getTopInventory().getContents()).filter(Objects::nonNull).filter(i -> !i.getType().isAir()).toList() );

                    int num = numOfOpedInventories.get(game.getName())-1;
                    if (num <= 0){
                        numOfOpedInventories.remove(game.getName());
                        cachedViews.remove(game.getName());
                    } else {
                        numOfOpedInventories.put(game.getName(), num);
                    }
                    break;
                }
            }
        }
    }
}
