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
import org.bukkit.entity.Frog;
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
            UUID_HITBOX_ENTITY_KEY = "uuid_hitbox",
            UUID_ARMORSTAND_KEY = "uuid_armorstand";

    //this number comes purely from testing with a small armor stand. Maybe one day a smart guy can add a fancy calculation why it is roughly this number.
    private final static double ARMORSTAND_OFFSET = 0.719;

    public static final NamespacedKey HIDDEN_KEY = new NamespacedKey(FindMe.inst(), "findMeGame");

    private final Team noCollistionTeam;

    private UUID uuidArmorStand;
    private UUID uuidHitBoxEntity;
    private boolean hasHead = false;
    private long cooldown = 0;

    private boolean hitBoxUpdated = false;
    private final static Class<? extends Entity> hitboxEntityClass = Frog.class;

    private Entity summonHitBoxEntity(@NotNull Location location, @NotNull String gameName){
        hitBoxUpdated = true;
        //get uuid and set all necessary properties of a freshly spawned hitBoxEntity
        //in other news: the settings are set with a lambda function, so no hitBoxEntity should ever flash for a moment.
        return location.getWorld().spawn(location, Frog.class, newFrog-> {
            newFrog.setInvisible(true);
            newFrog.setAI(false);
            newFrog.customName(Lang.build("findMe"));
            newFrog.setCustomNameVisible(false);
            newFrog.setCollidable(false);
            newFrog.setGravity(false);
            newFrog.setInvulnerable(true);
            //don't let this hitBoxEntity despawn.
            newFrog.setPersistent(true);
            newFrog.setSilent(true);

            //don't drop anything on death
            newFrog.setLootTable(LootTables.EMPTY.getLootTable());

            newFrog.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.STRING, gameName);

            noCollistionTeam.addEntity(newFrog);
            uuidHitBoxEntity = newFrog.getUniqueId();
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
            uuidArmorStand = newArmorStand.getUniqueId();
        });
    }

    public Hideaway(@NotNull Location location, @NotNull String gameName, @NotNull Team noCollistionTeam){
        this.noCollistionTeam = noCollistionTeam;
        summonHitBoxEntity(location, gameName);
        summonArmorStand(location, gameName);
    }

    private Hideaway(@NotNull UUID uuidArmorStand, @NotNull UUID uuidHitBoxEntity, @NotNull Team noCollistionTeam){
        this.noCollistionTeam = noCollistionTeam;
        this.uuidArmorStand = uuidArmorStand;
        this.uuidHitBoxEntity = uuidHitBoxEntity;
        Entity entity = Bukkit.getEntity(uuidArmorStand);

        ArmorStand armorStand = null;
        if (entity instanceof ArmorStand armorStand2){
            armorStand = armorStand2;
            noCollistionTeam.addEntity(armorStand2);
        }

        entity = Bukkit.getEntity(uuidHitBoxEntity);
        Entity hitBoxEntity = null;
        if (hitboxEntityClass.isInstance(entity)){
            hitBoxEntity = entity;
            noCollistionTeam.addEntity(entity);
        }

        // the armor stand and hitBoxEntity should never be separated. If they are we properly lost track of one of them.
        // so if one was loaded while the other wasn't, try to generate a new one of the other one
        if (armorStand == null && hitBoxEntity != null){
            String gameName = hitBoxEntity.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonArmorStand(hitBoxEntity.getLocation(), gameName);
            }
        } else if (armorStand != null && hitBoxEntity == null){
            String gameName = armorStand.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonHitBoxEntity(armorStand.getLocation().clone().add(0, ARMORSTAND_OFFSET, 0), gameName);
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
        resultMap.put(UUID_HITBOX_ENTITY_KEY, uuidHitBoxEntity.toString());
        resultMap.put(UUID_ARMORSTAND_KEY, uuidArmorStand.toString());
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

        obj = data.get(UUID_HITBOX_ENTITY_KEY);
        UUID hitBoxEntityUUID;
        if (obj instanceof String str2){
            if(UUID_PATTERN.matcher(str2).find()){
                hitBoxEntityUUID = UUID.fromString(str2);
            } else {
                GreenLogger.log(Level.WARNING, "couldn't deserialize uuid: " + str2 + ", skipping. Reason: not a valid uuid.");
                return null;
            }
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize uuid: " + obj + ", skipping. Reason: not a String.");
            return null;
        }

        return new Hideaway(armorStandUUID, hitBoxEntityUUID, noCollistionTeam);
    }

    public @Nullable ArmorStand getArmorStand() {
        //try to fetch the armor stand from bukkit
        Entity entity = Bukkit.getEntity(uuidArmorStand);

        if (entity instanceof ArmorStand armorStand){
            noCollistionTeam.addEntity(armorStand);

            return armorStand;
        } else {
            //try to get a new armor stand from the hitBoxEntity
            entity = Bukkit.getEntity(uuidHitBoxEntity);

            if (hitboxEntityClass.isInstance(entity)){
                String gameName = entity.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
                if (gameName != null){
                    return summonArmorStand(entity.getLocation(), gameName);
                }
            }
        }

        return null;
    }

    public @Nullable Entity getHitBoxEntity() {
        //try to fetch the hitBoxEntity from bukkit
        Entity entity = Bukkit.getEntity(uuidHitBoxEntity);

        if (hitboxEntityClass.isInstance(entity)){
            noCollistionTeam.addEntity(entity);

            return entity;
        } else {
            //try to get a hitBoxEntity stand from the armor stand
            Entity probablyArmorStand = Bukkit.getEntity(uuidArmorStand);

            if (probablyArmorStand instanceof ArmorStand armorStand){
                String gameName = armorStand.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
                if (gameName != null){
                    return summonHitBoxEntity(armorStand.getLocation().clone().add(0, ARMORSTAND_OFFSET, 0), gameName);
                }
            }
        }

        return null;
    }

    public @NotNull UUID getUUIDHitBox(){
        return uuidHitBoxEntity;
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

    public boolean isHitBoxUpdated() {
        return hitBoxUpdated;
    }

    /**
     * gives feedback that the uuid change of the hitBoxEntity was reflected in the tracking map
     * it's serves no functional purpose but helps to detect and correct errors.
     */
    protected void gotHitboxUpdate() {
        hitBoxUpdated = false;
    }

    public static Class<? extends Entity> getHitboxEntityClass(){
        return hitboxEntityClass;
    }
}
