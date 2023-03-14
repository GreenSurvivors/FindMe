package de.greensurvivors.greenfinder.dataObjects;

import de.greensurvivors.greenfinder.GreenFinder;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class HiddenStand {
    public static final NamespacedKey HIDDEN_KEY = new NamespacedKey(GreenFinder.inst(), "isHidden");

    private final ArmorStand armorStand;
    private long cooldown = 0;

    public HiddenStand(Location location){
        //get uuid and set all necessary properties of a freshly spawned armor stand
        //the number there comes purely from testing with a small armor stand. Maybe one day a smart guy can add a fancy calculation why it is roughly this number.
        //in other news: the settings are set with a lambda function, so no armor-stand should ever flash for a moment.
        armorStand = location.getWorld().spawn(location.clone().subtract(0, 0.719, 0), ArmorStand.class, newArmorStand -> {
            newArmorStand.setVisible(false);
            newArmorStand.setSmall(true);
            newArmorStand.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.LEGS, EquipmentSlot.FEET);
            newArmorStand.setCanMove(false);
            newArmorStand.setCanTick(false);
            newArmorStand.setCustomNameVisible(false);

            newArmorStand.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.INTEGER, 1);
        });
    }

    public HiddenStand(ArmorStand armorStand){
        this.armorStand = armorStand;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public UUID getUUID(){
        return armorStand.getUniqueId();
    }

    public boolean isCooldownOver(long millis) {
        return System.currentTimeMillis() > this.cooldown + millis;
    }

    public void setCooldownNow() {
        this.cooldown = System.currentTimeMillis();
    }
}
