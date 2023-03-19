package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.FindMe;
import de.greensurvivors.findme.GreenLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class HiddenStand implements ConfigurationSerializable {
    //pattern to mach if a string is a valid uuid
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\z");

    private final static String
            UUID_SLIME_KEY = "uuid_slime",
            UUID_ARMORSTAND_KEY = "uuid_armorstand";

    //this number comes purely from testing with a small armor stand. Maybe one day a smart guy can add a fancy calculation why it is roughly this number.
    private final static double ARMORSTAND_OFFSET = 0.719;

    public static final NamespacedKey HIDDEN_KEY = new NamespacedKey(FindMe.inst(), "findMeGame");

    private final Team noCollistionTeam;

    private UUID uuid_armorstand;
    private UUID uuid_slime;
    private ArmorStand armorStand;//might be null, if not loaded yet
    private Slime slime; //might be null, if not loaded yet
    private long cooldown = 0;

    private void summonSlime(@NotNull Location location, @NotNull String gameName){
        //get uuid and set all necessary properties of a freshly spawned slime
        //in other news: the settings are set with a lambda function, so no slime should ever flash for a moment.
        uuid_slime = location.getWorld().spawn(location, Slime.class, newSlime-> {
            newSlime.setInvisible(true);
            newSlime.setSize(1);
            newSlime.setAI(false);
            newSlime.setCustomNameVisible(false);
            newSlime.setCollidable(false);
            newSlime.setGravity(false);
            newSlime.setInvulnerable(true);
            //don't despawn this slime
            newSlime.setPersistent(true);

            newSlime.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.STRING, gameName);

            slime = newSlime;

            noCollistionTeam.addEntity(newSlime);
        }).getUniqueId();
    }

    private void summonArmorStand(@NotNull Location location, @NotNull String gameName){
        //get uuid and set all necessary properties of a freshly spawned armor stand
        //in other news: the settings are set with a lambda function, so no armor-stand should ever flash for a moment.
        uuid_armorstand = location.getWorld().spawn(location.clone().subtract(0, ARMORSTAND_OFFSET, 0), ArmorStand.class, newArmorStand -> {
            newArmorStand.setVisible(false);
            newArmorStand.setSmall(true);
            newArmorStand.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.LEGS, EquipmentSlot.FEET);
            newArmorStand.setCanMove(false);
            newArmorStand.setCanTick(false);
            newArmorStand.setCustomNameVisible(false);
            newArmorStand.setMarker(true);

            newArmorStand.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.STRING, gameName);

            armorStand = newArmorStand;

            noCollistionTeam.addEntity(newArmorStand);
        }).getUniqueId();
    }

    public HiddenStand(@NotNull Location location, @NotNull String gameName, @NotNull Team noCollistionTeam){
        this.noCollistionTeam = noCollistionTeam;
        summonSlime(location, gameName);
        summonArmorStand(location, gameName);
    }

    public HiddenStand(@NotNull UUID uuid_armorstand, @NotNull UUID uuid_slime, @NotNull Team noCollistionTeam){
        this.noCollistionTeam = noCollistionTeam;
        this.uuid_armorstand = uuid_armorstand;
        this.uuid_slime = uuid_slime;
        Entity entity = Bukkit.getEntity(uuid_armorstand);

        if (entity instanceof ArmorStand armorStand2){
            this.armorStand = armorStand2;
            noCollistionTeam.addEntity(armorStand2);
        }
        entity = Bukkit.getEntity(uuid_slime);

        if (entity instanceof Slime slime2){
            this.slime = slime2;
            noCollistionTeam.addEntity(slime2);
        }

        // the armor stand and slime should never be separated. If they are we properly lost track of one of them.
        // so if one was loaded while the other wasn't, try to generate a new one of the other one
        if (this.armorStand == null && this.slime != null){
            String gameName = this.slime.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonArmorStand(this.slime.getLocation(), gameName);
            }
        } else if (this.armorStand != null && this.slime == null){
            String gameName = this.armorStand.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonArmorStand(this.armorStand.getLocation().clone().add(0, ARMORSTAND_OFFSET, 0), gameName);
            }
        }
    }

    /**
     * Creates a Map representation of this class.
     *
     * @return Map containing the current state of this class
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(UUID_SLIME_KEY, uuid_slime.toString());
        resultMap.put(UUID_ARMORSTAND_KEY, uuid_armorstand.toString());
        return resultMap;
    }

    /**
     * Creates new instance of a Map representation of this class.
     *
     * @return a new instance
     */
    public static @Nullable HiddenStand deserialize(@NotNull Map<String, Object> data, Team noCollistionTeam) {
        Object obj = data.get(UUID_ARMORSTAND_KEY);
        UUID armorStandUUID;
        if (obj instanceof String str){
            if(UUID_PATTERN.matcher(str).find()){
                armorStandUUID = UUID.fromString(str);
            } else {
                GreenLogger.log(Level.WARNING, "couldn't deserialize uuid: " + str + ", skipping. Reason: not a valid uuid.");
                return null;
            }
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize uuid: " + obj + ", skipping. Reason: not a String.");
            return null;
        }

        obj = data.get(UUID_SLIME_KEY);
        UUID slimeUUID;
        if (obj instanceof String str2){
            if(UUID_PATTERN.matcher(str2).find()){
                slimeUUID = UUID.fromString(str2);
            } else {
                GreenLogger.log(Level.WARNING, "couldn't deserialize uuid: " + str2 + ", skipping. Reason: not a valid uuid.");
                return null;
            }
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize uuid: " + obj + ", skipping. Reason: not a String.");
            return null;
        }

        return new HiddenStand(armorStandUUID, slimeUUID, noCollistionTeam);
    }

    public @Nullable ArmorStand getArmorStand() {
        if (armorStand == null || !armorStand.isValid()){
            //try to fetch the entity from bukkit
            Entity entity = Bukkit.getEntity(uuid_armorstand);

            if (entity instanceof ArmorStand armorStand2){
                this.armorStand = armorStand2;
                noCollistionTeam.addEntity(armorStand2);
            }
        }

        return armorStand;
    }

    public @Nullable Slime getSlime() {
        if (slime == null || !slime.isValid()){
            //try to fetch the entity from bukkit
            Entity entity = Bukkit.getEntity(uuid_slime);

            if (entity instanceof Slime slime2){
                this.slime = slime2;
                noCollistionTeam.addEntity(slime2);
            }
        }

        return slime;
    }

    public @NotNull UUID getUUIDSlime(){
        return uuid_slime;
    }

    public boolean isCooldownOver(long millis) {
        return System.currentTimeMillis() > this.cooldown + millis;
    }

    public void setCooldownNow() {
        this.cooldown = System.currentTimeMillis();
    }
}
