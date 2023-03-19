package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.FindMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HiddenStand {
    public static final NamespacedKey HIDDEN_KEY = new NamespacedKey(FindMe.inst(), "isHidden");

    private final UUID uuid;
    private ArmorStand armorStand;//might be null, if not loaded yet
    private long cooldown = 0;

    public HiddenStand(Location location){
        //get uuid and set all necessary properties of a freshly spawned armor stand
        //the number there comes purely from testing with a small armor stand. Maybe one day a smart guy can add a fancy calculation why it is roughly this number.
        //in other news: the settings are set with a lambda function, so no armor-stand should ever flash for a moment.
        uuid = location.getWorld().spawn(location.clone().subtract(0, 0.719, 0), ArmorStand.class, newArmorStand -> {
            newArmorStand.setVisible(false);
            newArmorStand.setSmall(true);
            newArmorStand.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.LEGS, EquipmentSlot.FEET);
            newArmorStand.setCanMove(false);
            newArmorStand.setCanTick(false);
            newArmorStand.setCustomNameVisible(false);

            newArmorStand.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.INTEGER, 1);

            armorStand = newArmorStand;
        }).getUniqueId();
    }

    public HiddenStand(@NotNull UUID uuid){
        this.uuid = uuid;
        Entity entity = Bukkit.getEntity(uuid);

        if (entity instanceof ArmorStand armorStand2){
            this.armorStand = armorStand2;
        }
    }

    public @Nullable ArmorStand getArmorStand() {
        if (armorStand == null){
            //try to fetch the entity from bukkit
            Entity entity = Bukkit.getEntity(uuid);

            if (entity instanceof ArmorStand armorStand2){
                this.armorStand = armorStand2;
            }
        }

        return armorStand;
    }

    public @NotNull UUID getUUID(){
        return uuid;
    }

    public boolean isCooldownOver(long millis) {
        return System.currentTimeMillis() > this.cooldown + millis;
    }

    public void setCooldownNow() {
        this.cooldown = System.currentTimeMillis();
    }
}
