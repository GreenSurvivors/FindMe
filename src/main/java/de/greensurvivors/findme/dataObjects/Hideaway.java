package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.FindMe;
import de.greensurvivors.findme.GreenLogger;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Hideaway implements ConfigurationSerializable {
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
    private boolean hasHead = false;
    private long cooldown = 0;

    private Slime summonSlime(@NotNull Location location, @NotNull String gameName){
        //get uuid and set all necessary properties of a freshly spawned slime
        //in other news: the settings are set with a lambda function, so no slime should ever flash for a moment.
        return location.getWorld().spawn(location, Slime.class, newSlime-> {
            newSlime.setInvisible(true);
            newSlime.setSize(1);
            newSlime.setAI(false);
            newSlime.customName(Lang.build("findMe"));
            newSlime.setCustomNameVisible(false);
            newSlime.setCollidable(false);
            newSlime.setGravity(false);
            newSlime.setInvulnerable(true);
            //don't despawn this slime
            newSlime.setPersistent(true);

            //don't drop anything on death
            newSlime.setLootTable(LootTables.EMPTY.getLootTable());

            newSlime.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.STRING, gameName);

            noCollistionTeam.addEntity(newSlime);
            uuid_slime = newSlime.getUniqueId();
        });
    }

    private ArmorStand summonArmorStand(@NotNull Location location, @NotNull String gameName){
        //get uuid and set all necessary properties of a freshly spawned armor stand
        //in other news: the settings are set with a lambda function, so no armor-stand should ever flash for a moment.
        return location.getWorld().spawn(location.clone().subtract(0, ARMORSTAND_OFFSET, 0), ArmorStand.class, newArmorStand -> {
            newArmorStand.setVisible(false);
            newArmorStand.setSmall(true);
            newArmorStand.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.LEGS, EquipmentSlot.FEET);
            newArmorStand.setCanMove(false);
            newArmorStand.setCanTick(false);
            newArmorStand.customName(Lang.build("findMe"));
            newArmorStand.setCustomNameVisible(false);
            newArmorStand.setCustomNameVisible(false);
            newArmorStand.setMarker(true);

            newArmorStand.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.STRING, gameName);

            noCollistionTeam.addEntity(newArmorStand);
            uuid_armorstand = newArmorStand.getUniqueId();
        });
    }

    public Hideaway(@NotNull Location location, @NotNull String gameName, @NotNull Team noCollistionTeam){
        this.noCollistionTeam = noCollistionTeam;
        summonSlime(location, gameName);
        summonArmorStand(location, gameName);
    }

    public Hideaway(@NotNull UUID uuid_armorstand, @NotNull UUID uuid_slime, @NotNull Team noCollistionTeam){
        this.noCollistionTeam = noCollistionTeam;
        this.uuid_armorstand = uuid_armorstand;
        this.uuid_slime = uuid_slime;
        Entity entity = Bukkit.getEntity(uuid_armorstand);

        ArmorStand armorStand = null;
        if (entity instanceof ArmorStand armorStand2){
            armorStand = armorStand2;
            noCollistionTeam.addEntity(armorStand2);
        }

        entity = Bukkit.getEntity(uuid_slime);
        Slime slime = null;
        if (entity instanceof Slime slime2){
            slime = slime2;
            noCollistionTeam.addEntity(slime2);
        }

        // the armor stand and slime should never be separated. If they are we properly lost track of one of them.
        // so if one was loaded while the other wasn't, try to generate a new one of the other one
        if (armorStand == null && slime != null){
            String gameName = slime.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonArmorStand(slime.getLocation(), gameName);
            }
        } else if (armorStand != null && slime == null){
            String gameName = armorStand.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonSlime(armorStand.getLocation().clone().add(0, ARMORSTAND_OFFSET, 0), gameName);
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
    public static @Nullable Hideaway deserialize(@NotNull Map<String, Object> data, Team noCollistionTeam) {
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

        return new Hideaway(armorStandUUID, slimeUUID, noCollistionTeam);
    }

    public @Nullable ArmorStand getArmorStand() {
        //try to fetch the armor stand from bukkit
        Entity entity = Bukkit.getEntity(uuid_armorstand);

        if (entity instanceof ArmorStand armorStand){
            noCollistionTeam.addEntity(armorStand);

            return armorStand;
        } else {
            //try to get a new armor stand from the slime
            entity = Bukkit.getEntity(uuid_slime);

            if (entity instanceof Slime slime){
                String gameName = slime.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
                if (gameName != null){
                    return summonArmorStand(slime.getLocation(), gameName);
                }
            }
        }

        return null;
    }

    public @Nullable Slime getSlime() {
        //try to fetch the slime from bukkit
        Entity entity = Bukkit.getEntity(uuid_slime);

        if (entity instanceof Slime slime){
            noCollistionTeam.addEntity(slime);

            return slime;
        } else {
            //try to get a slime stand from the armor stand
            entity = Bukkit.getEntity(uuid_armorstand);

            if (entity instanceof ArmorStand armorStand){
                String gameName = armorStand.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
                if (gameName != null){
                    return summonSlime(armorStand.getLocation().clone().add(0, ARMORSTAND_OFFSET, 0), gameName);
                }
            }
        }

        return null;
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

    public boolean hasHead() {
        return hasHead;
    }

    public void setHasHead(boolean hasHead) {
        this.hasHead = hasHead;
    }
}
