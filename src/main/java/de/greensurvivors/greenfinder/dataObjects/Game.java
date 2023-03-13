package de.greensurvivors.greenfinder.dataObjects;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.GreenLogger;
import de.greensurvivors.greenfinder.Utils;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.*;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Game {
    public enum GameStates {
        ACTIVE("active"),
        STARTING("starting"),
        ENDED("ended");

        private final String name;

        GameStates(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final NamespacedKey HIDDEN_KEY = new NamespacedKey(GreenFinder.inst(), "isHidden");

    private final int STARTING_HIDDEN_PERCENT = 75;

    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective;

    private final HashMap<UUID, ArmorStand> hiddenStands = new HashMap<>(); //cache so we don't have to lookup everytime we need the entity.
    private final LinkedHashSet<ItemStack> heads = new LinkedHashSet<>();

    private final HashSet<Player> players = new HashSet<>();

    private GameStates gameState = GameStates.ENDED;
    private final String name;
    private boolean allowLateJoin = false;

    private Location lobbyLoc;
    private Location startLoc;
    private Location quitLoc;

    private long gameTimeLength = 5*60*20;
    private byte remainingCountdownSeconds = 0;
    private long remainingGameTime = 0;

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

            newArmorStand.getPersistentDataContainer().set(HIDDEN_KEY, PersistentDataType.INTEGER, 1);
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

    public void playerJoin(Player player){
        players.add(player);
        player.sendMessage("welcome in game finders. use /gf quit to exit the game.");
        player.sendMessage("objective message");

        if (gameState.equals(GameStates.ACTIVE)){
            player.setScoreboard(scoreboard);

            if (startLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            } else {
                GreenLogger.log(Level.WARNING, "No start postion was given for finder game \"" + name + "\". Couldn't teleport anybody.");
            }
        } else {
            if (lobbyLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            } else {
                GreenLogger.log(Level.WARNING, "No lobby postion was given for finder game \"" + name + "\". Couldn't teleport anybody.");
            }
        }
    }

    public void playerQuit(Player player){
        players.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        if (quitLoc != null){
            if (Utils.isPaper()){
                player.teleportAsync(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                player.teleport(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        } else {
            GreenLogger.log(Level.WARNING, "No quit postion was given for finder game \"" + name + "\". Couldn't teleport anybody.");
        }
    }

    public void startup(){
        gameState = GameStates.STARTING;

        remainingCountdownSeconds = 10;
        Bukkit.getScheduler().runTaskLater(GreenFinder.inst(), this::startingCountdown, 20);
    }

    private void startingCountdown(){
        remainingCountdownSeconds--;
        if (remainingCountdownSeconds <= 0){
            startMain();
        } else {
            Bukkit.getScheduler().runTaskLater(GreenFinder.inst(), this::startingCountdown, 20);

            for (Player player : players){
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.8f, 1.0f);
            }
        }
    }

    private void GameTimer(){
        remainingGameTime--;

        TimeHelper timeHelper = new TimeHelper(remainingGameTime);
        boolean shouldMakeCountdownNoise = (remainingGameTime <= TimeHelper.TICKS_PER_SECOND * 10) && (remainingGameTime % 20 == 0);

        for (Player player : players){
            player.sendActionBar(Lang.build(timeHelper.getMinutes() + " : " + timeHelper.getSeconds()));

            if (shouldMakeCountdownNoise){
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.8f, 1.0f);
            }
        }

        if (remainingGameTime >= 0){
            Bukkit.getScheduler().runTaskLater(GreenFinder.inst(), this::GameTimer, 1);
        }
    }

    private void startMain (){
        gameState = GameStates.ACTIVE;
        Random random = new Random();
        ItemStack[] headArray = heads.toArray(new ItemStack[0]);
        final int max = headArray.length -1;

        for (ArmorStand stand : hiddenStands.values()){
            if (random.nextInt(100) <= STARTING_HIDDEN_PERCENT){
                stand.setItem(EquipmentSlot.HEAD, headArray[random.nextInt(max)]);
            } else {
                stand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR, 1));
            }
        }

        for (Player player : players){
            player.setScoreboard(scoreboard);

            if (startLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }

        if (startLoc == null){
            GreenLogger.log(Level.WARNING, "No start postion was given for finder game \"" + name + "\". Couldn't teleport anybody.");
        }

        remainingGameTime = gameTimeLength;
        Bukkit.getScheduler().runTaskLater(GreenFinder.inst(), this::GameTimer, 1);
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

            if (quitLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }

        if (quitLoc == null){
            GreenLogger.log(Level.WARNING, "No quitting postion was given for finder game \"" + name + "\". Couldn't teleport anybody.");
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

    public boolean isAllowLateJoin() {
        return allowLateJoin;
    }

    public void setAllowLateJoin(boolean allowLateJoin) {
        this.allowLateJoin = allowLateJoin;
    }

    public Location getLobbyLoc() {
        return lobbyLoc;
    }

    public void setLobbyLoc(Location lobbyLoc) {
        this.lobbyLoc = lobbyLoc;
    }

    public Location getStartLoc() {
        return startLoc;
    }

    public void setStartLoc(Location startLoc) {
        this.startLoc = startLoc;
    }

    public Location getQuitLoc() {
        return quitLoc;
    }

    public void setQuitLoc(Location quitLoc) {
        this.quitLoc = quitLoc;
    }

    public long getGameTimeLength() {
        return gameTimeLength;
    }

    public void setGameTimeLength(long gameTimeLength){
        this.gameTimeLength = gameTimeLength;
    }

    public long getRemainingGameTime(){
        return remainingGameTime;
    }
}
