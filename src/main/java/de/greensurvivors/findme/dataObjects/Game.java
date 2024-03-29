package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.FindMe;
import de.greensurvivors.findme.GreenLogger;
import de.greensurvivors.findme.Utils;
import de.greensurvivors.findme.config.MainConfig;
import de.greensurvivors.findme.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Game implements ConfigurationSerializable {

    //all known game states
    public enum GameStates {
        ACTIVE("active"),
        STARTING("starting"),
        ENDED("ended");

        //name to have a defined to string
        private final String name;

        GameStates(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    //shown scoreboard
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective;

    //set of all known uuids of the hitBoxEntity part of all hideaways.
    //the Class tracks the cooldown for hiding
    private final HashMap<UUID, Hideaway> hideaways = new HashMap<>();
    private int hideawaysWithHeadNum = 0;
    //set of all items that can get hidden away
    private final LinkedHashSet<ItemStack> heads = new LinkedHashSet<>();

    //set of players ingame
    private final HashSet<Player> players = new HashSet<>();

    //state the game is currently in
    private GameStates gameState = GameStates.ENDED;
    //name of the game
    private final String name;
    //if players can join after the game has startet
    private boolean allowLateJoin = false;

    //location of the lobby
    private Location lobbyLoc;
    //location the player will teleport to on the start of the game
    private Location startLoc;
    //location the player will teleport to on end of the game or when they quit
    private Location quitLoc;

    //how many percent of all hideaways get a head at game start
    private Double startingHiddenPercent = 75.0;
    //how many percent of all hideaways can get a head while the game is currently running
    private Double maximumThresholdPercent = 100.0;
    //how many ticks on average will pass until a hideaways gets a new head
    private long averageHideTicks = 1200;
    //how long the cooldown is, while a hideaway cant get a new head
    private long HideCooldownMillis = 20000;

    //how long a game is until it automatically ends
    //default value is 5 minutes
    private long gameTimeLength = TimeUnit.MINUTES.toSeconds(5)*TimeHelper.TICKS_PER_SECOND;
    //internal value how many seconds remain in the starting countdown of the game
    private byte remainingCountdownSeconds = 0;
    //how long the game will last, will be set to gameTimeLength at the start of the game
    private long remainingGameTime = 0;
    private final static DecimalFormat timeSecondsFormat = new DecimalFormat("");


    /**(de)serialisation keys **/
    private final static String STARTING_HIDDEN_PERCENT_KEY = "starting_hidden_percent";
    private final static String MAXIMUM_THRESHOLD_PERCENT_KEY = "maximum_threshold_percent";
    private final static String AVERAGE_HIDE_TICKS_KEY = "average_hide_ticks";
    private final static String HIDE_COOLDOWN_Millis_KEY = "hide_cooldown_millis";
    private final static String HEADS_KEY = "heads";
    private final static String HIDEAWAY_KEY = "hideaways";
    private final static String NAME_KEY = "name";
    private final static String ALLOW_LATE_JOIN_KEY = "allowLateJoin";
    private final static String LOBBY_LOC_KEY = "lobbyloc";
    private final static String START_LOC_KEY = "startloc";
    private final static String QUIT_LOC_KEY = "quitloc";
    private final static String GAME_TIME_LENGTH_KEY = "game_time_length";

    /**
     * only proper way to create a new findMe! game. (except for deserialization)
     * saves the game to the disc
     * @param name of the game
     */
    public Game(@NotNull String name){
        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, PlainTextComponentSerializer.plainText().deserialize(name).color(NamedTextColor.GOLD));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.name = name;

        MainConfig.inst().saveGame(this);
    }

    /**
     * deserialization constructor. only for internal use.
     * @param startingHiddenPercent
     * @param averageHideTicks
     * @param HideCooldownMillis
     * @param hideaways
     * @param heads
     * @param name
     * @param allowLateJoin
     * @param lobbyLoc
     * @param startLoc
     * @param quitLoc
     * @param gameTimeLength
     */
    private Game(double startingHiddenPercent, double maximum_threshold_percent,
                 int averageHideTicks, int HideCooldownMillis,
                 @NotNull HashMap<UUID, Hideaway> hideaways, @NotNull LinkedHashSet<ItemStack> heads,
                 @NotNull String name,
                 boolean allowLateJoin,
                 @Nullable Location lobbyLoc, @Nullable Location startLoc, @Nullable Location quitLoc,
                 long gameTimeLength){

        this.hideaways.putAll(hideaways);
        this.heads.addAll(heads);

        this.name = name;

        this.allowLateJoin = allowLateJoin;

        this.lobbyLoc = lobbyLoc;
        this.startLoc = startLoc;
        this.quitLoc = quitLoc;

        this.startingHiddenPercent = startingHiddenPercent;
        this.maximumThresholdPercent = maximum_threshold_percent;
        this.averageHideTicks = averageHideTicks;
        this.HideCooldownMillis = HideCooldownMillis;

        this.gameTimeLength = gameTimeLength;

        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Lang.build(name));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        //save a potential new entity of the hideaway list or a new version of serialization of future updates
        MainConfig.inst().saveGame(this);
    }

    /**
     * serialize the game to a map of keys and simple objects
     * @return
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(STARTING_HIDDEN_PERCENT_KEY, String.valueOf(startingHiddenPercent));
        result.put(MAXIMUM_THRESHOLD_PERCENT_KEY, maximumThresholdPercent);
        result.put(AVERAGE_HIDE_TICKS_KEY, averageHideTicks);
        result.put(HIDE_COOLDOWN_Millis_KEY, HideCooldownMillis);
        result.put(HIDEAWAY_KEY, hideaways.values().stream().map(Hideaway::serialize).collect(Collectors.toList()));
        result.put(HEADS_KEY, heads.stream().filter(Objects::nonNull).map(ItemStack::serialize).collect(Collectors.toList()));
        result.put(NAME_KEY, name);
        result.put(ALLOW_LATE_JOIN_KEY, allowLateJoin);
        if (lobbyLoc != null)
            result.put(LOBBY_LOC_KEY, lobbyLoc.serialize());
        if (startLoc != null)
            result.put(START_LOC_KEY, startLoc.serialize());
        if (quitLoc != null)
            result.put(QUIT_LOC_KEY, quitLoc.serialize());
        result.put(GAME_TIME_LENGTH_KEY, gameTimeLength);

        return result;
    }

    /**
     * deserializes a map of keys and simple objects as produced by serialize() into values accepted by the deserialization constructor
     * @param data
     * @return
     */
    public static Game deserialize(@NotNull Map<String, Object> data) {
        Object temp;

        // <editor-fold defaultstate="collapsed" desc="deserialize">
        temp = data.get(NAME_KEY);
        String name;
        if (temp instanceof String tempName){
            name = tempName;
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize name: " + ". Reason: is not a string.");
            return null;
        }

        temp = data.get(STARTING_HIDDEN_PERCENT_KEY);
        double starting_hidden_percent;
        if (temp instanceof Double tempDouble){
            starting_hidden_percent = tempDouble;
        } else if (temp instanceof Integer tempInt){
            starting_hidden_percent = tempInt;
        } else if (temp instanceof String str){
            if (Utils.isDouble(str)) {
                starting_hidden_percent = Double.parseDouble(str);
            } else if (Utils.isInt(str)){
                starting_hidden_percent = Integer.parseInt(str);
            } else {
                GreenLogger.log(Level.SEVERE, "couldn't deserialize starting_hidden_percent: " + temp + " of game " + name);
                return null;
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize starting_hidden_percent: " + temp + " of game " + name);
            return null;
        }

        temp = data.get(MAXIMUM_THRESHOLD_PERCENT_KEY);
        double maximum_threshold_percent;
        if (temp == null) { // this was later introduced and needs a default for old games.
            maximum_threshold_percent = 100;
        } else if (temp instanceof Double tempDouble){
            maximum_threshold_percent = tempDouble;
        } else if (temp instanceof Integer tempInt){
            maximum_threshold_percent = tempInt;
        } else if (temp instanceof String str){
            if (Utils.isDouble(str)) {
                maximum_threshold_percent = Double.parseDouble(str);
            } else if (Utils.isInt(str)){
                maximum_threshold_percent = Integer.parseInt(str);
            } else {
                GreenLogger.log(Level.SEVERE, "couldn't deserialize maximum_threshold_percent: " + temp + " of game " + name);
                return null;
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize starting_hidden_percent: " + temp + " of game " + name);
            return null;
        }

        temp = data.get(HIDE_COOLDOWN_Millis_KEY);
        int hideCooldown_millis;
        if (temp instanceof Integer tempInt){
            hideCooldown_millis = tempInt;
        } else if (temp instanceof String str && Utils.isInt(str)){
            hideCooldown_millis = Integer.parseInt(str);
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize hideCooldown_millis: " + temp + " of game " + name);
            return null;
        }

        temp = data.get(AVERAGE_HIDE_TICKS_KEY);
        int average_hide_ticks;
        if (temp instanceof Integer tempInt){
            average_hide_ticks = tempInt;
        } else if (temp instanceof String str && Utils.isInt(str)){
            average_hide_ticks = Integer.parseInt(str);
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize average_hide_ticks: " + temp + " of game " + name);
            return null;
        }

        temp = data.get(HIDEAWAY_KEY);
        HashMap<UUID, Hideaway> hideaways_ = new HashMap<>();
        if (temp instanceof List<?> objList){
            for (Object obj: objList){

                if (obj instanceof Hideaway hideaway) {
                    hideaways_.put(hideaway.getUUIDHitBox(), hideaway);
                } else if (obj instanceof Map<?, ?> map){
                    Map<String, Object> hideawaysMap = new HashMap<>();
                    for (Object obj2: map.keySet()){
                        if (obj2 instanceof String str){
                            hideawaysMap.put(str, map.get(obj2));
                        } else {
                            GreenLogger.log(Level.WARNING, "couldn't deserialize hideaway property: " + obj2 + " of game " + name + ", skipping. Reason: not a string.");
                        }
                    }
                    Hideaway hideaway = Hideaway.deserialize(hideawaysMap);
                    if (hideaway == null){
                        break;
                    }

                    hideaways_.put(hideaway.getUUIDHitBox(), hideaway);
                    hideaway.gotHitboxUpdate();
                } else if (obj instanceof MemorySection memorySection){
                    Hideaway hideaway = Hideaway.deserialize(memorySection.getValues(false));
                    if (hideaway == null){
                        break;
                    }
                    hideaways_.put(hideaway.getUUIDHitBox(), hideaway);
                    hideaway.gotHitboxUpdate();
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize hideaway: " + obj + " of game " + name + ", skipping. Reason: unknown type.");
                }
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize uuid list: " + temp + " of game " + name);
            return null;
        }

        temp = data.get(HEADS_KEY);
        LinkedHashSet<ItemStack> heads = new LinkedHashSet<ItemStack>();
        if (temp instanceof List<?> objList2){
            for (Object obj: objList2){
                if (obj instanceof ItemStack itemStack) {
                    heads.add(itemStack);
                } else if (obj instanceof Map<?,?> map){
                    Map<String, Object> itemStackMap = new HashMap<>();
                    for (Object obj2: map.keySet()){
                        if (obj2 instanceof String str){
                            itemStackMap.put(str, map.get(obj2));
                        } else {
                            GreenLogger.log(Level.WARNING, "couldn't deserialize head item property: " + obj2 + " of game " + name + ", skipping. Reason: not a string.");
                        }
                    }
                    heads.add(ItemStack.deserialize(itemStackMap));
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize head item: " + obj + " of game " + name + ", skipping. Reason: not a item stack nor map.");
                }
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize head list: " + temp + " of game " + name);
            return null;
        }

        temp = data.get(ALLOW_LATE_JOIN_KEY);
        Boolean allowLateJoin;
        if (temp instanceof Boolean tempBool){
            allowLateJoin = tempBool;
        } else if (temp instanceof String str){
            allowLateJoin = BooleanUtils.toBooleanObject(str);

            if (allowLateJoin == null){
                GreenLogger.log(Level.SEVERE, "couldn't deserialize allowLateJoin: " + str + " of game " + name + ". Reason: string is not a bool.");
                return null;
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize allowLateJoin bool: " + temp + " of game " + name);
            return null;
        }

        temp = data.get(LOBBY_LOC_KEY);
        Location lobbyLoc = null;
        if (temp instanceof Map<?, ?> map){
            Map<String, Object> stringObjectMap = new HashMap<>();

            for (Object obj: map.keySet()){
                if (obj instanceof String str){
                    stringObjectMap.put(str, map.get(obj));
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize lobby location property: " + obj + " of game " + name + ", skipping. Reason: not a string.");
                }
            }
            lobbyLoc = Location.deserialize(stringObjectMap);
        } else if (temp instanceof MemorySection memorySection) {
            lobbyLoc = Location.deserialize(memorySection.getValues(false));
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize lobby location: " + temp + " of game " + name);
        }

        temp = data.get(START_LOC_KEY);
        Location startLoc = null;
        if (temp instanceof Map<?, ?> map2){
            Map<String, Object> stringObjectMap = new HashMap<>();

            for (Object obj: map2.keySet()){
                if (obj instanceof String str){
                    stringObjectMap.put(str, map2.get(obj));
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize start location property: " + obj + " of game " + name + ", skipping. Reason: not a string.");
                }
            }
            startLoc = Location.deserialize(stringObjectMap);
        } else if (temp instanceof MemorySection memorySection) {
            startLoc = Location.deserialize(memorySection.getValues(false));
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize start location: " + temp + " of game " + name);
        }

        temp = data.get(QUIT_LOC_KEY);
        Location quitLoc = null;
        if (temp instanceof Map<?, ?> map3){
            Map<String, Object> stringObjectMap = new HashMap<>();

            for (Object obj: map3.keySet()){
                if (obj instanceof String str){
                    stringObjectMap.put(str, map3.get(obj));
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize quit property: " + obj + " of game " + name + ", skipping. Reason: not a string.");
                }
            }
            quitLoc = Location.deserialize(stringObjectMap);
        } else if (temp instanceof MemorySection memorySection) {
            quitLoc = Location.deserialize(memorySection.getValues(false));
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize quit location: " + temp + " of game " + name);
        }

        temp = data.get(GAME_TIME_LENGTH_KEY);
        long game_time_length;
        if (temp instanceof Long tempLong){
            game_time_length = tempLong;
        } else if(temp instanceof String str){
            if (Utils.islong(str)){
                game_time_length = Long.parseLong(str);
            } else {
                GreenLogger.log(Level.SEVERE, "couldn't deserialize game time length long: " + str + " of game " + name + " not a long! (1)");
                return null;
            }
        } else if (temp instanceof Integer tempLong){
            game_time_length = tempLong;
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize game time length long: " + temp + " of game " + name + " not a long! (2)");
            return null;
        }
        // </editor-fold>

        return new Game(starting_hidden_percent,
                maximum_threshold_percent,
                average_hide_ticks, hideCooldown_millis,
                hideaways_, heads,
                name,
                allowLateJoin,
                lobbyLoc, startLoc, quitLoc,
                game_time_length);

    }

    /**
     * creates a new hide away at the given location and keeps track of it (saves the game)
     * @param location
     */
    public void addHideaway(@NotNull Location location){
        Hideaway hideaway = new Hideaway(location, name);
        hideaways.put(hideaway.getUUIDHitBox(), hideaway);
        hideaway.gotHitboxUpdate();

        MainConfig.inst().saveGame(this);
    }

    /**
     * gives all hideaways around the given location a glowing effect for 10s
     * @param location location around all hideaways will get the glowing effect
     * @param range range how far will be searched for hideaways
     */
    public void showAroundLocation(@NotNull Location location, int range){
        World world = location.getWorld();

        for (final Hideaway hideaway : hideaways.values()){
            Entity hitboxEntity = hideaway.getHitBoxEntity();

            if (hitboxEntity != null){
                Location hidingLoc = hitboxEntity.getLocation();

                if (world != hidingLoc.getWorld())
                    continue;

                if (NumberConversions.square(range) >= (NumberConversions.square(location.getX() - hidingLoc.getX()) +
                        NumberConversions.square(location.getY() - hidingLoc.getY()) +
                        NumberConversions.square(location.getZ() - hidingLoc.getZ()))){
                    hideaway.setHitBoxInvisible(false);

                    Bukkit.getScheduler().runTaskLater(FindMe.inst(), () ->
                            hideaway.setHitBoxInvisible(true), 400);
                }
            }
        }
    }

    /**
     * get the nearest hideaway in a range of 5 blocks
     * @param startingPos location around the nearest hideing place will be searched
     * @return nearest hideaway or null if no where found
     */
    public @Nullable Hideaway getNearestHideaway(@NotNull Location startingPos){
        Hideaway result = null;
        double lastDistance = Double.MAX_VALUE;

        Collection<Interaction> nearbyHitBoxEntities = startingPos.getNearbyEntitiesByType(Interaction.class, 5);

        Set<Hideaway> nearbyHidingPlaces = nearbyHitBoxEntities.stream().
                //get all tracked hit box entities that are in radius
                map(s -> hideaways.get(s.getUniqueId())).filter(Objects::nonNull).
                collect(Collectors.toSet());

        for(Hideaway hideaway : nearbyHidingPlaces) {
            //we don't need the root, if we only want to know what entity is further
            //also, hideaway.getHitBoxEntity is backed up by getting the initial entities from getNearbyEntitiesByType,
            //so it never will be null.
            double distance = startingPos.distanceSquared(hideaway.getHitBoxEntity().getLocation());
            if(distance < lastDistance) {
                lastDistance = distance;
                result = hideaway;
            }
        }

        return result;
    }

    /**
     * removes a hidden interaction + hitBoxEntity pair and no longer keeps track of it (saves the game)
     * @param uuid of a hitBoxEntity the interaction entity belongs to
     */
    public void removeHideaway(@NotNull UUID uuid){
        Hideaway hideaway =  hideaways.get(uuid);
        //kill the entities
        if (hideaway != null){
            Entity hitBoxEntity = hideaway.getHitBoxEntity();
            if (hitBoxEntity != null){
                hitBoxEntity.remove();;
            }

            Display displayEntity = hideaway.getItemDisplay();
            if (displayEntity != null){
                displayEntity.remove();
            }
        }

        hideaways.remove(uuid);

        MainConfig.inst().saveGame(this);
    }

    protected void removeAllHideaways() {
        for (Hideaway hideaway : hideaways.values()){
            //kill the entities
            if (hideaway != null) {
                Entity hitBoxEntity = hideaway.getHitBoxEntity();
                if (hitBoxEntity != null) {
                    hitBoxEntity.remove();
                }

                Display displayEntity = hideaway.getItemDisplay();
                if (displayEntity != null) {
                    displayEntity.remove();
                }
            }
        }

        hideaways.clear();
    }

    /**
     * forces the game to end and clears up the tracked data.
     * Will get the game into a broken state and is only for shutting doen
     */
    protected void clear(){
        end();

        hideaways.clear();
        heads.clear();

        objective.unregister();
    }

    /**
     * handles a player joining a game.
     * so add them into the set of tracked players, show the scoreboard and teleport whenever possible
     * @param player who joins
     */
    protected void playerJoin(@NotNull Player player){
        players.add(player);
        player.sendMessage(Lang.build(Lang.MESSAGE_JOIN.get()));
        player.sendMessage(Lang.build(Lang.MESSAGE_OBJECTIVE.get()));

        if (gameState.equals(GameStates.ACTIVE)){
            player.setScoreboard(scoreboard);

            if (startLoc != null){
                GameManager.inst().addTeleportingPlayer(player);

                startLoc.getWorld().getChunkAtAsync(startLoc).thenAccept(chunk -> {
                    player.teleport(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    GameManager.inst().removeTeleportingPlayer(player);
                });
            } else {
                GreenLogger.log(Level.WARNING, "No start postion was given for FindMe! game \"" + name + "\". Couldn't teleport anybody.");
            }
        } else {
            if (lobbyLoc != null){
                GameManager.inst().addTeleportingPlayer(player);

                lobbyLoc.getWorld().getChunkAtAsync(lobbyLoc).thenAccept(chunk -> {
                    player.teleport(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    GameManager.inst().removeTeleportingPlayer(player);
                });
            } else {
                GreenLogger.log(Level.WARNING, "No lobby postion was given for FindMe! game \"" + name + "\". Couldn't teleport anybody.");
            }
        }
    }

    /**
     * handles a player quitting the game.
     * removes them from the set of tracked players and teleports them if possible
     * @param player who quits
     */
    protected void playerQuit(@NotNull Player player){
        players.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        if (quitLoc != null){
            GameManager.inst().addTeleportingPlayer(player);

            quitLoc.getWorld().getChunkAtAsync(quitLoc).thenAccept(chunk -> {
                player.teleport(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                GameManager.inst().removeTeleportingPlayer(player);
            });
        } else {
            GreenLogger.log(Level.WARNING, "No quit postion was given for FindMe! game \"" + name + "\". Couldn't teleport anybody.");
        }

        if (players.isEmpty()){
            end();
        }
    }

    /**
     * start up of the game.
     * Starts the countdown after witch the game automaticly starts.
     */
    public void startup(){
        gameState = GameStates.STARTING;

        remainingCountdownSeconds = 10;
        Bukkit.getScheduler().runTaskLater(FindMe.inst(), this::startingCountdown, 20);
    }

    /**
     * this function will call itself every second for however long the starting countdown is supposed to be.
     * after that it will start the game
     */
    private void startingCountdown(){
        remainingCountdownSeconds--;

        if (remainingCountdownSeconds <= 0){
            startMain();
        } else {
            Bukkit.getScheduler().runTaskLater(FindMe.inst(), this::startingCountdown, 20);

            for (Player player : players){
                player.showTitle(Title.title(Lang.build(String.valueOf(remainingCountdownSeconds)), Lang.build("")));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.8f, 1.0f);
            }
        }
    }

    /**
     * main loop of the findMe! game, calls itself every tick while the game is running
     * shows the remaining time and handles hiding new stuff in the hideaways
     * after the time runs out it will end the game.
     */
    private void GameTimer(){
        remainingGameTime--;

        if (remainingGameTime % 20 == 0){
            TimeHelper timeHelper = new TimeHelper(remainingGameTime);
            boolean shouldMakeCountdownNoise = (remainingGameTime <= TimeHelper.TICKS_PER_SECOND * 10);

            for (Player player : players){
                //show remaining game time
                player.sendActionBar(Lang.build(timeHelper.getMinutes() + " : " + timeSecondsFormat.format(timeHelper.getSeconds())));

                //end countdown
                if (shouldMakeCountdownNoise){
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.8f, 1.0f);
                }
            }
        }

        //hide new stuff in hideaways, if they are not in cooldown
        //only try to hide if we have heads to begin with
        final int numOfHeads = heads.size();
        if (numOfHeads > 0){

            Random random = new Random();
            ItemStack[] headArray = heads.toArray(new ItemStack[0]);

            double cachedNumberOfHideaways = hideaways.size();
            for (Hideaway hideaway : hideaways.values()){
                // skip adding more if we already reached the threshold
                if (cachedNumberOfHideaways / hideawaysWithHeadNum * 100.0D > maximumThresholdPercent) {
                    break;
                }

                if (hideaway.getItemDisplay() != null && ! hideaway.hasHead()) {
                    if (hideaway.isCooldownOver(HideCooldownMillis) && random.nextLong(averageHideTicks) == 1){
                        hideawaysWithHeadNum++;
                        hideaway.setHasHead(true);
                        hideaway.getItemDisplay().setItemStack(headArray[random.nextInt(numOfHeads)]);
                    }
                }
            }
        }

        //next game tick or end of game
        if (remainingGameTime >= 0){
            Bukkit.getScheduler().runTaskLater(FindMe.inst(), this::GameTimer, 1);
        } else {
            //---- broadcast the game results ----

            if (!players.isEmpty()) {
                //create body of players and their scores
                LinkedHashSet<Component> lines =
                        //sort the players by their score (Playerscore is just a record, that implements the comparable interface for the score value aka Int)
                        players.stream().map(player -> new PlayerScore(player.getName(), objective.getScore(player).getScore())).sorted().
                        //map players and their scores into a component
                                map(playerScore -> Lang.build(Lang.GAME_END_SCORE_PLAYER.get().replace(Lang.TYPE, playerScore.name()).replace(Lang.VALUE, String.valueOf(playerScore.score()))))
                        //collect the stream into a set, so it can get joined together into one component
                        //LinkedHashSet, so it is guarantied to stay sorted
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                //broadcast the scoreboard objective
                Bukkit.broadcast(
                        //join header with body
                        Component.join(
                                JoinConfiguration.separator(Component.newline()),
                                //header
                                Lang.build(Lang.GAME_END_SCORE_HEADER.get()),
                                //body
                                //make a big component from a collection of components of the player scores
                                Component.join(JoinConfiguration.separator(Lang.build(", ")), lines)));
            }
            // ---- end of broadcast ----

            //end the game
            end();
        }
    }

    /**
     * start of the main phase of the game.
     * set random heads for the configurated percentage of hideaways and show scoreboard;
     * teleport all players to starting location if possible, and start main loop
     */
    private void startMain (){
        //be sure to reset every score
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        gameState = GameStates.ACTIVE;
        final int max = heads.size();
        //only set heads if the set isn't empty
        if (max > 0){
            Random random = new Random();
            ItemStack[] headArray = heads.toArray(new ItemStack[max]);
            hideawaysWithHeadNum = 0;

            Set<Hideaway> updatedHideaways = new HashSet<>();

            for (Hideaway hideaway : hideaways.values()) {
                hideaway.getHitBoxEntity();
                if (hideaway.isHitBoxUpdated()) {
                    updatedHideaways.add(hideaway);
                }

                if (hideaway.getItemDisplay() != null){
                    if (random.nextInt(100) <= startingHiddenPercent) {
                        hideaway.getItemDisplay().setItemStack(headArray[random.nextInt(max)]);
                        hideawaysWithHeadNum++;
                        hideaway.setHasHead(true);
                    } else {
                        hideaway.getItemDisplay().setItemStack(new ItemStack(Material.AIR, 1));
                        hideaway.setHasHead(false);
                    }
                }

                hideaway.setCooldownNow();
            }

            //update the uuids
            for (Hideaway hideaway : updatedHideaways){
                hideaways.entrySet().removeIf(entry -> entry.getValue() == hideaway);
                hideaways.put(hideaway.getUUIDHitBox(), hideaway);

                hideaway.gotHitboxUpdate();
            }
            if (updatedHideaways.size() > 0){
                MainConfig.inst().saveGame(this);
            }
        }

        if(startLoc != null && !startLoc.getChunk().isLoaded()){
            startLoc.getChunk().load();
        }

        for (Player player : players){
            player.setScoreboard(scoreboard);

            if (startLoc != null){
                GameManager.inst().addTeleportingPlayer(player);

                startLoc.getWorld().getChunkAtAsync(startLoc).thenAccept(chunk -> {
                    player.teleport(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    GameManager.inst().removeTeleportingPlayer(player);
                });
            }
        }

        if (startLoc == null){
            GreenLogger.log(Level.WARNING, "No start postion was given for FindMe! game \"" + name + "\". Couldn't teleport anybody.");
        }

        remainingGameTime = gameTimeLength;
        Bukkit.getScheduler().runTaskLater(FindMe.inst(), this::GameTimer, 1);
    }

    /**
     * called whenever a player finds a hideaway (technically the hitBoxEntity of it), increments the score of the player
     * @param player the player who found the hideaway
     * @param uuid uuid of the hitBoxEntity part of a hideaway
     */
    public void findHideaway(@NotNull Player player, @NotNull UUID uuid){
        Hideaway hideaway = hideaways.get(uuid);

        if (hideaway == null || hideaway.getItemDisplay() == null){
            return;
        }

        //don't react to empty hideaways
        if (hideaway.hasHead()){
            hideawaysWithHeadNum--;
            hideaway.setHasHead(false);
            hideaway.setCooldownNow();

            //remove head & play sound
            hideaway.getItemDisplay().setItemStack(new ItemStack(Material.AIR, 1));
            hideaway.getItemDisplay().getLocation().getWorld().playSound(hideaway.getItemDisplay(), Sound.ENTITY_ITEM_PICKUP,0.9f, 0.9f);

            addScore(player, 1);
        }
    }

    /**
     * gets the current state the game is in
     * @return active, starting up or ended (e.a. not running)
     */
    public GameStates getGameState (){
        return gameState;
    }

    /**
     * adds an increment to the current score of a player
     * @param player
     * @param increment
     */
    public void addScore(@NotNull Player player, int increment){
        Score score = objective.getScore(player);
        score.setScore(score.getScore() + increment);
    }

    /**
     * ends the game, removes scoreboard, trys to teleport all players to the quit location, sets the game state to ended
     */
    public void end(){
        //reset score of everyone even if they already leaved the game
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        for (Player player : players){
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

            if (quitLoc != null){
                GameManager.inst().addTeleportingPlayer(player);
                quitLoc.getWorld().getChunkAtAsync(quitLoc).thenAccept(chunk -> {
                    player.teleport(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    GameManager.inst().removeTeleportingPlayer(player);
                });
            }

            GameManager.inst().playersGameEnded(player);
        }

        if (gameState != GameStates.ENDED && quitLoc == null){
            GreenLogger.log(Level.WARNING, "No quitting postion was given for FindMe! game \"" + name + "\". Couldn't teleport anybody.");
        }

        players.clear();

        gameState = GameStates.ENDED;
        remainingGameTime = 0;
    }

    /**
     * @return the name of the game
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * @return returns the list of all head, guaranties the same order every time the function gets called,
     * so the gui is nice and ordered
     */
    public @NotNull LinkedHashSet<ItemStack> getHeads() {
        return heads;
    }

    /**
     * sets the set of heads, called by the head gui
     * @param heads
     */
    public void setHeads(@NotNull Collection<ItemStack> heads){
        this.heads.clear();
        this.heads.addAll(heads);

        MainConfig.inst().saveGame(this);
    }

    /**
     * @return true if joining is allowed, after a game has already started
     */
    public boolean isAllowLateJoin() {
        return allowLateJoin;
    }

    /**
     * set if joining is allowed after the game already started
     * @param allowLateJoin
     */
    public void setAllowLateJoin(boolean allowLateJoin) {
        this.allowLateJoin = allowLateJoin;

        MainConfig.inst().saveGame(this);
    }

    /**
     * get the location of the lobby, might be null if no where defined
     * @return
     */
    public @Nullable Location getLobbyLoc() {
        return lobbyLoc;
    }

    /**
     * set the location of the lobby
     * @param lobbyLoc
     */
    public void setLobbyLoc(@Nullable Location lobbyLoc) {
        this.lobbyLoc = lobbyLoc;

        MainConfig.inst().saveGame(this);
    }

    /**
     * get the location the player will be teleported to, when the game starts. might be null
     */
    public @Nullable Location getStartLoc() {
        return startLoc;
    }

    /**
     * set the start location
     * @param startLoc
     */
    public void setStartLoc(@Nullable Location startLoc) {
        this.startLoc = startLoc;

        MainConfig.inst().saveGame(this);
    }

    /**
     * get the location the player will be teleported to, when the game ends or the player quits. might be null
     * @return
     */
    public @Nullable Location getQuitLoc() {
        return quitLoc;
    }

    /**
     * set the end / quit location
     * @param quitLoc
     */
    public void setQuitLoc(@NotNull Location quitLoc) {
        this.quitLoc = quitLoc;

        MainConfig.inst().saveGame(this);
    }

    /**
     * get the time length a game lasts
     * @return
     */
    public long getGameTimeLength() {
        return gameTimeLength;
    }

    /**
     * set how long a game lasts
     * @param gameTimeLength
     */
    public void setGameTimeLength(long gameTimeLength){
        this.gameTimeLength = gameTimeLength;

        MainConfig.inst().saveGame(this);
    }

    /**
     * set how many ticks pass on average until a hideaway gets its head back
     * @param ticks
     */
    public void setAverageHideTicks(long ticks) {
        this.averageHideTicks = ticks;
    }

    /**
     * get how many ticks pass on average until a hideaway gets its head back
     * @return
     */
    public long getAverageHideTicks(){
        return this.averageHideTicks;
    }

    /**
     * set how much percent of all hideaways will have a head when the game starts
     * @param percent 0 - 100, all values above 100 will act the same as 100 and all values under 0 will be the same as 0
     */
    public void setStartingHiddenPercent(double percent) {
        this.startingHiddenPercent = percent;
    }

    /**
     * gets how much percent of all hideaways will have a head when the game starts
     * @return
     */
    public double getStartingHiddenPercent(){
        return this.startingHiddenPercent;
    }

    /**
     * set how much percent of all hideaways will get a head resupplied while the game is running at max
     * @param percent 0 - 100, all values above 100 will act the same as 100 and all values under 0 will be the same as 0
     */
    public void setMaximumThresholdPercent(double percent) {
        this.maximumThresholdPercent = percent;
    }

    /**
     * gets how much percent of all hideaways will get a head resupplied at max while the game is running
     * @return
     */
    public Double getMaximumThresholdPercent() {
        return maximumThresholdPercent;
    }

    /**
     * set how long a hideaway is on cooldown and can not get its head back, after it was found
     * @param millis
     */
    public void setHideCooldown(long millis){
        this.HideCooldownMillis = millis;
    }

    /**
     * get how long a hideaway is on cooldown can not get its head back, after it was found
     * @return
     */
    public long getHideCooldown(){
        return this.HideCooldownMillis;
    }

    /**
     * get how long the active game will still be running for. will be 0, if it isn't running.
     * @return
     */
    public long getRemainingGameTime(){
        return remainingGameTime;
    }

    public int getNumOfPlayers(){
        return players.size();
    }

    public int getNumOfHeads(){
        return heads.size();
    }
}
