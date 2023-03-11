package de.greensurvivors.greenfinder.dataObjects;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.*;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    public enum GameStates {
        ACTIVE,
        STARTING,
        ENDED
    }

    public static final NamespacedKey hiddenKey = new NamespacedKey(GreenFinder.inst(), "isHidden");

    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective;

    private final HashMap<UUID, ArmorStand> hiddenStands = new HashMap<>(); //cache so we don't have to lookup everytime we need the entity.

    private final HashSet<Player> players = new HashSet<>();

    private GameStates gameState = GameStates.ENDED;
    private final String name;

    private final LinkedHashSet<ItemStack> heads = new LinkedHashSet<>();

    public Game(String name){
        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Lang.build(name));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.name = name;
    }

    public void addHiddenStand(Location location){
        //get uuid and set all necessary properties of a freshly spawned armor stand
        //the number there comes purely from testing with a small armor stand. Maybe one day a smart guy can add a fancy calculation why it is roughly this number.
        //in other news: the settings are set with a lambda function, so no armor-stand should ever flash for a moment.
        ArmorStand hiddenStand = location.getWorld().spawn(location.clone().subtract(0, 0.719, 0), ArmorStand.class, newArmorStand -> {
            newArmorStand.setVisible(false);
            newArmorStand.setSmall(true);
            newArmorStand.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.LEGS, EquipmentSlot.FEET);
            newArmorStand.setCanMove(false);
            newArmorStand.setCanTick(false);
            newArmorStand.setCustomNameVisible(false);

            newArmorStand.getPersistentDataContainer().set(hiddenKey, PersistentDataType.INTEGER, 1);
        });

        UUID uuid = hiddenStand.getUniqueId();
        Entity entity = Bukkit.getEntity(uuid);

        if (entity instanceof ArmorStand armorStand){
            hiddenStands.put(uuid, armorStand);
        }
    }

    public void showAroundLocation(Location location, Integer range){
        World world = location.getWorld();

        for (ArmorStand armorStand : hiddenStands.values()){
            Location standLoc = armorStand.getLocation();

            if (world != standLoc.getWorld())
                continue;

            if (NumberConversions.square(range) >= (NumberConversions.square(location.getX() - standLoc.getX()) +
                    NumberConversions.square(location.getY() - standLoc.getY()) +
                    NumberConversions.square(location.getZ() - standLoc.getZ()))){
                armorStand.setGlowing(true);

                Bukkit.getScheduler().runTaskLater(GreenFinder.inst(), () ->{
                    armorStand.setGlowing(false);
                }, 200);
            }
        }
    }

    public ArmorStand getNearestStand(Location startingPos){
        ArmorStand result = null;
        double lastDistance = Double.MAX_VALUE;

        Collection<ArmorStand> nearbyStands = startingPos.getNearbyEntitiesByType(ArmorStand.class, 5);
        nearbyStands = nearbyStands.stream().filter(s -> hiddenStands.get(s.getUniqueId()) != null).collect(Collectors.toSet());

        for(ArmorStand armorStand : nearbyStands) {
            double distance = startingPos.distanceSquared(armorStand.getLocation());
            if(distance < lastDistance) {
                lastDistance = distance;
                result = armorStand;
            }
        }

        return result;
    }

    public void removeHiddenStand(UUID uuid){
        hiddenStands.remove(uuid);
    }

    protected void clear(){
        end();

        hiddenStands.clear();
    }

    protected void playerJoin(Player player){
        players.add(player);
    }

    protected void playerQuit(Player player){
        players.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void startup(){
        gameState = GameStates.STARTING;

    }

    public void startMain (){
        gameState = GameStates.ACTIVE;

        for (Player player : players){
            player.setScoreboard(scoreboard);
        }
    }

    public void findStand(UUID uuid){
        ArmorStand hiddenStand = hiddenStands.get(uuid);

        if (hiddenStand == null){
            return;
        }

        //remove head & play sound
        hiddenStand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR, 1));
        hiddenStand.getLocation().getWorld().playSound(hiddenStand, Sound.ENTITY_ITEM_PICKUP,0.9f, 0.9f);
    }

    public GameStates getGameState (){
        return gameState;
    }

    public void addScore(Player player, Integer increment){
        Score score = objective.getScore(player);
        score.setScore(score.getScore() + increment);
    }

    protected void end(){
        gameState = GameStates.ENDED;

        for (Player player : players){
            scoreboard.resetScores(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }

        players.clear();
    }
    public @NotNull String getName() {
        return name;
    }

    public @NotNull LinkedHashSet<ItemStack> getHeads() {
        return heads;
    }

    public void setHeads(Collection<ItemStack> heads){
        this.heads.clear();
        this.heads.addAll(heads);

    }
}
