package de.greensurvivors.greenfinder.dataObjects;

import de.greensurvivors.greenfinder.GreenFinder;
import de.greensurvivors.greenfinder.GreenLogger;
import de.greensurvivors.greenfinder.Utils;
import de.greensurvivors.greenfinder.language.Lang;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Game implements ConfigurationSerializable {

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

    private final int STARTING_HIDDEN_PERCENT;
    private final int AVERAGE_TICKS_UNTIL_REHEAD;
    private final int MIN_Millis_UNTIL_REHEAD;

    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective;

    private final HashMap<UUID, HiddenStand> hiddenStands = new HashMap<>(); //cache so we don't have to lookup everytime we need the entity.
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

        STARTING_HIDDEN_PERCENT = 75;
        AVERAGE_TICKS_UNTIL_REHEAD = 600;
        MIN_Millis_UNTIL_REHEAD = 10000;
    }

    private Game(int startingHiddenPercent, int averageTicksUntilRehead, int minMillisUntilRehead,
                 @NotNull HashMap<UUID, HiddenStand> hiddenStands, @NotNull LinkedHashSet<ItemStack> heads,
                 @NotNull String name,
                 boolean allowLateJoin,
                 @NotNull Location lobbyLoc, @NotNull Location startLoc, @NotNull Location quitLoc,
                 long gameTimeLength){

        this.STARTING_HIDDEN_PERCENT = startingHiddenPercent;
        this.AVERAGE_TICKS_UNTIL_REHEAD = averageTicksUntilRehead;
        this.MIN_Millis_UNTIL_REHEAD = minMillisUntilRehead;

        this.hiddenStands.putAll(hiddenStands);
        this.heads.addAll(heads);

        this.name = name;

        this.allowLateJoin = allowLateJoin;

        this.lobbyLoc = lobbyLoc;
        this.startLoc = startLoc;
        this.quitLoc = quitLoc;

        this.gameTimeLength = gameTimeLength;

        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Lang.build(name));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("starting_hidden_percent", STARTING_HIDDEN_PERCENT);
        result.put("average_ticks_until_rehead", AVERAGE_TICKS_UNTIL_REHEAD);
        result.put("min_milli_rehead_cooldown", MIN_Millis_UNTIL_REHEAD);
        result.put("uuids", hiddenStands.keySet().stream().map(UUID::toString));
        result.put("heads", heads.stream().map(ItemStack::serialize));
        result.put("name", name);
        result.put("allowLateJoin", allowLateJoin);
        result.put("lobbyloc", lobbyLoc.serialize());
        result.put("startloc", startLoc.serialize());
        result.put("quitloc", quitLoc.serialize());
        result.put("game_time_length", gameTimeLength);

        return result;
    }

    public static Game deserialize(@NotNull Map<String, Object> data) {
        Object temp;

        temp = data.get("starting_hidden_percent");
        int starting_hidden_percent;
        if (temp instanceof Integer a){
            starting_hidden_percent = a;
        } else if (temp instanceof String b && Utils.isInt(b)){
            starting_hidden_percent = Integer.parseInt(b);
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize starting_hidden_percent: " + temp);
            return null;
        }

        temp = data.get("min_milli_rehead_cooldown");
        int min_milli_rehead_cooldown;
        if (temp instanceof Integer a){
            min_milli_rehead_cooldown = a;
        } else if (temp instanceof String b && Utils.isInt(b)){
            min_milli_rehead_cooldown = Integer.parseInt(b);
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize min_milli_rehead_cooldown: " + temp);
            return null;
        }

        temp = data.get("average_ticks_until_rehead");
        int average_ticks_until_rehead;
        if (temp instanceof Integer a){
            average_ticks_until_rehead = a;
        } else if (temp instanceof String b && Utils.isInt(b)){
            average_ticks_until_rehead = Integer.parseInt(b);
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize average_ticks_until_rehead: " + temp);
            return null;
        }

        return Game(starting_hidden_percent, average_ticks_until_rehead, min_milli_rehead_cooldown, );

    }

    public void addHiddenStand(Location location){
        HiddenStand hiddenStand = new HiddenStand(location);
        hiddenStands.put(hiddenStand.getUUID(), hiddenStand);

    }

    public void showAroundLocation(Location location, Integer range){
        World world = location.getWorld();

        for (HiddenStand hiddenStand : hiddenStands.values()){
            Location standLoc = hiddenStand.getArmorStand().getLocation();

            if (world != standLoc.getWorld())
                continue;

            if (NumberConversions.square(range) >= (NumberConversions.square(location.getX() - standLoc.getX()) +
                    NumberConversions.square(location.getY() - standLoc.getY()) +
                    NumberConversions.square(location.getZ() - standLoc.getZ()))){
                hiddenStand.getArmorStand().setGlowing(true);

                Bukkit.getScheduler().runTaskLater(GreenFinder.inst(), () ->{
                    hiddenStand.getArmorStand().setGlowing(false);
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

        Random random = new Random();
        ItemStack[] headArray = heads.toArray(new ItemStack[0]);
        final int max = headArray.length -1;

        for (HiddenStand hiddenStand : hiddenStands.values()){
            if (!hiddenStand.getArmorStand().getEquipment().getHelmet().getType().isItem() &&
                    hiddenStand.isCooldownOver(MIN_Millis_UNTIL_REHEAD) &&
                    random.nextInt(AVERAGE_TICKS_UNTIL_REHEAD) == 1){
                hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, headArray[random.nextInt(max)]);
                hiddenStand.setCooldownNow();
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

        for (HiddenStand hiddenStand : hiddenStands.values()){
            if (random.nextInt(100) <= STARTING_HIDDEN_PERCENT){
                hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, headArray[random.nextInt(max)]);
            } else {
                hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR, 1));
            }

            hiddenStand.setCooldownNow();
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
        HiddenStand hiddenStand = hiddenStands.get(uuid);

        if (hiddenStand == null){
            return;
        }

        //remove head & play sound
        hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR, 1));
        hiddenStand.getArmorStand().getLocation().getWorld().playSound(hiddenStand.getArmorStand(), Sound.ENTITY_ITEM_PICKUP,0.9f, 0.9f);
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
