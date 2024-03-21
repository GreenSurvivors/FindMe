package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.FindMe;
import de.greensurvivors.findme.GreenLogger;
import de.greensurvivors.findme.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
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
            UUID_ITEM_DISPLAY_KEY = "uuid_display";

    public static final NamespacedKey HIDDEN_KEY = new NamespacedKey(FindMe.inst(), "findMeGame");

    private static final Float CUBE_SIZE = 0.5f;

    private UUID uuidDisplay;
    private UUID uuidHitBoxEntity;
    private boolean hasHead = false;
    private long cooldown = 0;

    private boolean hitBoxUpdated = false;
    private String gameName;

    /**
     * The new entities don't have a way to make them permanent invisible since they aren't living entities.
     * without it, players can find the hideaways easy with their hit boxes
     * So we have to set it via reflection our own.
     * @param entity the entity that should get invisible
     */
    private void setEntityInvisible(@NotNull Entity entity, boolean invisible){
        net.minecraft.world.entity.Entity handle = (net.minecraft.world.entity.Entity)Reflection.getHandle(entity);

        if (handle != null){
            //skip CraftEntity and use the mns entity directly via reflection.
            //this makes the permanent entity (in)visible
            handle.persistentInvisibility = invisible;
            handle.setSharedFlag(5, invisible);
        }
    }


    private Entity summonInteractionEntity(@NotNull Location location){
        hitBoxUpdated = true;
        //get uuid and set all necessary properties of a freshly spawned hitBoxEntity
        //in other news: the settings are set with a lambda function, so no hitBoxEntity should ever flash for a moment.
        return location.getWorld().spawn(location, Interaction.class, newInteraction-> {
            newInteraction.customName(Lang.build("findMe"));
            newInteraction.setCustomNameVisible(false);
            //don't let this hitBoxEntity despawn.
            newInteraction.setPersistent(true);
            newInteraction.setInteractionWidth(CUBE_SIZE);
            newInteraction.setInteractionHeight(CUBE_SIZE);
            newInteraction.setResponsive(true);

            setEntityInvisible(newInteraction, true);

            //I have no idea why every other entity rotates with its spawn location but Interaction and display entities are not.
            newInteraction.setRotation(location.getYaw(), 0);

            newInteraction.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.STRING, this.gameName);

            uuidHitBoxEntity = newInteraction.getUniqueId();
        });
    }

    private ItemDisplay summonDisplay(@NotNull Location location){
        //get uuid and set all necessary properties of a freshly spawned display entity
        //in other news: the settings are set with a lambda function, so no entity should ever flash for a moment.
        //for reasons unknown to man, the location of this entity is not at its feed, but directly at its middle point.
        //so we have to summon it half its height up.
        return location.getWorld().spawn(location.clone().add(0, CUBE_SIZE / 2, 0), ItemDisplay.class, newDisplay -> {
            newDisplay.customName(Lang.build("findMe"));
            newDisplay.setCustomNameVisible(false);
            newDisplay.setCustomNameVisible(false);
            newDisplay.setInvulnerable(true);
            newDisplay.setPersistent(true);

            //turn shadow off
            newDisplay.setShadowRadius(0f);
            //display all blocks round about head (block entity) size
            newDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            //set if the displayed item should rotate when the player moves
            newDisplay.setBillboard(Display.Billboard.FIXED);

            //I have no idea why every other entity rotates with its spawn location but Interaction and display entities are not.
            //only god knows why the shown item is displayed backwards
            newDisplay.setRotation((location.getYaw() + 180) % 360, 0);

            //turn line of sight off
            setEntityInvisible(newDisplay, true);

            newDisplay.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.STRING, this.gameName);

            uuidDisplay = newDisplay.getUniqueId();
        });
    }

    public Hideaway(@NotNull Location location, @NotNull String gameName){
        this.gameName = gameName;
        summonInteractionEntity(location);
        summonDisplay(location);
    }

    private Hideaway(@NotNull UUID uuidDisplay, @NotNull UUID uuidHitBoxEntity){
        this.uuidDisplay = uuidDisplay;
        this.uuidHitBoxEntity = uuidHitBoxEntity;
        Entity entity = Bukkit.getEntity(uuidDisplay);

        ItemDisplay itemDisplay = null;
        if (entity instanceof ItemDisplay itemDisplay2){
            itemDisplay = itemDisplay2;
        }

        entity = Bukkit.getEntity(uuidHitBoxEntity);
        Entity hitBoxEntity = null;
        if (entity instanceof Interaction){
            hitBoxEntity = entity;
        }

        // the interaction entity and hitBoxEntity should never be separated. If they are we properly lost track of one of them.
        // so if one was loaded while the other wasn't, try to generate a new one of the other one
        if (itemDisplay == null && hitBoxEntity != null){
            String gameName = hitBoxEntity.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonDisplay(hitBoxEntity.getLocation());
            }
        } else if (itemDisplay != null && hitBoxEntity == null){
            String gameName = itemDisplay.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
            if (gameName != null){
                summonInteractionEntity(itemDisplay.getLocation());
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
        resultMap.put(UUID_ITEM_DISPLAY_KEY, uuidDisplay.toString());
        return resultMap;
    }

    /**
     * Creates new instance of a Map representation of this class.
     *
     * @return a new instance
     */
    public static @Nullable Hideaway deserialize(@NotNull Map<String, Object> data) {
        Object obj = data.get(UUID_ITEM_DISPLAY_KEY);
        UUID displayUUID;
        if (obj instanceof String str){
            if(UUID_PATTERN.matcher(str).find()){
                displayUUID = UUID.fromString(str);
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

        return new Hideaway(displayUUID, hitBoxEntityUUID);
    }

    public @Nullable ItemDisplay getItemDisplay() {
        //try to fetch display entity from bukkit
        Entity entity = Bukkit.getEntity(uuidDisplay);

        if (entity instanceof ItemDisplay itemDisplay){
            return itemDisplay;
        } else {
            //try to get a new interaction entity from the hitBoxEntity
            entity = Bukkit.getEntity(uuidHitBoxEntity);

            if (entity instanceof Interaction){
                String gameName = entity.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
                if (gameName != null){
                    return summonDisplay(entity.getLocation());
                }
            }
        }

        return null;
    }

    public @Nullable Entity getHitBoxEntity() {
        //try to fetch the hitBoxEntity from bukkit
        Entity entity = Bukkit.getEntity(uuidHitBoxEntity);

        if (entity instanceof Interaction){
            return entity;
        } else {
            //try to get a hitBoxEntity from the interaction entity
            Entity probablyDisplay = Bukkit.getEntity(uuidDisplay);

            if (probablyDisplay instanceof ItemDisplay itemDisplay){
                String gameName = itemDisplay.getPersistentDataContainer().get(HIDDEN_KEY, PersistentDataType.STRING);
                if (gameName != null){
                    return summonInteractionEntity(itemDisplay.getLocation());
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

    public void setHitBoxInvisible(boolean invisible){
        Entity hitBoxEntity = getHitBoxEntity();

        if(hitBoxEntity != null){
            setEntityInvisible(hitBoxEntity, invisible);
        }
    }
}
