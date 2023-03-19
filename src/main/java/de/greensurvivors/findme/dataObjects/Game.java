package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.FindMe;
import de.greensurvivors.findme.GreenLogger;
import de.greensurvivors.findme.Utils;
import de.greensurvivors.findme.config.MainConfig;
import de.greensurvivors.findme.language.Lang;
import net.kyori.adventure.title.Title;
import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final Team noCollisionTeam;

    //set of all known uuids of all hidden stands.
    //the Class Hiddenstand just caches the armorstand and tracks the cooldown for reheading
    private final HashMap<UUID, HiddenStand> hiddenStands = new HashMap<>();
    //set of all items an armor stand can wear on its head
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

    //how many percent of all hidden stands get a head at game start
    private Double startingHiddenPercent = 75.0;
    //how many ticks on average will pass until an armor stand gets a new head
    private long averageTicksUntilRehead = 1200;
    //how long the cooldown is, while a hidden stand cant get a new head
    private long minMillisUntilRehead = 20000;

    //how long a game is until it automatically ends
    //default value is 5 minutes
    private long gameTimeLength = TimeUnit.MINUTES.toSeconds(5)*TimeHelper.TICKS_PER_SECOND;
    //internal value how many seconds remain in the starting countdown of the game
    private byte remainingCountdownSeconds = 0;
    //how long the game will last, will be set to gameTimeLength at the start of the game
    private long remainingGameTime = 0;

    /**(de)serialisation keys **/
    private final static String STARTING_HIDDEN_PERCENT_KEY = "starting_hidden_percent";
    private final static String AVERAGE_TICKS_UNTIL_REHEAD_KEY = "average_ticks_until_rehead";
    private final static String MIN_Millis_UNTIL_REHEAD_KEY = "min_milli_rehead_cooldown";
    private final static String HEADS_KEY = "heads";
    private final static String HIDDEN_STANDS_KEY = "hidden_stands";
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
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Lang.build(name));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.noCollisionTeam = scoreboard.registerNewTeam("noCollision");
        this.noCollisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        this.noCollisionTeam.setCanSeeFriendlyInvisibles(false);

        this.name = name;

        MainConfig.inst().saveGame(this);
    }

    /**
     * deserialization constructor. only for internal use.
     * @param scoreboard
     * @param noCollisionTeam
     * @param startingHiddenPercent
     * @param averageTicksUntilRehead
     * @param minMillisUntilRehead
     * @param hiddenStands
     * @param heads
     * @param name
     * @param allowLateJoin
     * @param lobbyLoc
     * @param startLoc
     * @param quitLoc
     * @param gameTimeLength
     */
    private Game(Scoreboard scoreboard, Team noCollisionTeam,
                 double startingHiddenPercent, int averageTicksUntilRehead, int minMillisUntilRehead,
                 @NotNull HashMap<UUID, HiddenStand> hiddenStands, @NotNull LinkedHashSet<ItemStack> heads,
                 @NotNull String name,
                 boolean allowLateJoin,
                 @Nullable Location lobbyLoc, @Nullable Location startLoc, @Nullable Location quitLoc,
                 long gameTimeLength){

        this.scoreboard = scoreboard;
        this.noCollisionTeam = noCollisionTeam;

        this.hiddenStands.putAll(hiddenStands);
        this.heads.addAll(heads);

        this.name = name;

        this.allowLateJoin = allowLateJoin;

        this.lobbyLoc = lobbyLoc;
        this.startLoc = startLoc;
        this.quitLoc = quitLoc;

        this.startingHiddenPercent = startingHiddenPercent;
        this.averageTicksUntilRehead = averageTicksUntilRehead;
        this.minMillisUntilRehead = minMillisUntilRehead;

        this.gameTimeLength = gameTimeLength;

        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Lang.build(name));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        noCollisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    /**
     * serialize the game to a map of keys and simple objects
     * @return
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(STARTING_HIDDEN_PERCENT_KEY, String.valueOf(startingHiddenPercent));
        result.put(AVERAGE_TICKS_UNTIL_REHEAD_KEY, averageTicksUntilRehead);
        result.put(MIN_Millis_UNTIL_REHEAD_KEY, minMillisUntilRehead);
        result.put(HIDDEN_STANDS_KEY, hiddenStands.values().stream().map(HiddenStand::serialize).collect(Collectors.toList()));
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
        Scoreboard scoreboard_ = Bukkit.getScoreboardManager().getNewScoreboard();
        Team noCollisionTeam_ = scoreboard_.registerNewTeam("noCollision");
        noCollisionTeam_.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        noCollisionTeam_.setCanSeeFriendlyInvisibles(false);

        Object temp;

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
                GreenLogger.log(Level.SEVERE, "couldn't deserialize starting_hidden_percent: " + temp);
                return null;
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize starting_hidden_percent: " + temp);
            return null;
        }

        temp = data.get(MIN_Millis_UNTIL_REHEAD_KEY);
        int min_milli_rehead_cooldown;
        if (temp instanceof Integer tempInt){
            min_milli_rehead_cooldown = tempInt;
        } else if (temp instanceof String str && Utils.isInt(str)){
            min_milli_rehead_cooldown = Integer.parseInt(str);
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize min_milli_rehead_cooldown: " + temp);
            return null;
        }

        temp = data.get(AVERAGE_TICKS_UNTIL_REHEAD_KEY);
        int average_ticks_until_rehead;
        if (temp instanceof Integer tempInt){
            average_ticks_until_rehead = tempInt;
        } else if (temp instanceof String str && Utils.isInt(str)){
            average_ticks_until_rehead = Integer.parseInt(str);
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize average_ticks_until_rehead: " + temp);
            return null;
        }

        temp = data.get(HIDDEN_STANDS_KEY);
        HashMap<UUID, HiddenStand> hiddenStands = new HashMap<>();
        if (temp instanceof List<?> objList){
            for (Object obj: objList){

                if (obj instanceof HiddenStand hiddenStand) {
                    hiddenStands.put(hiddenStand.getUUIDSlime(), hiddenStand);
                } else if (obj instanceof Map<?, ?> map){
                    Map<String, Object> hiddenStandMap = new HashMap<>();
                    for (Object obj2: map.keySet()){
                        if (obj2 instanceof String str){
                            hiddenStandMap.put(str, map.get(obj2));
                        } else {
                            GreenLogger.log(Level.WARNING, "couldn't deserialize hidden stand property: " + obj2 + ", skipping. Reason: not a string.");
                        }
                    }
                    HiddenStand hiddenStand = HiddenStand.deserialize(hiddenStandMap, noCollisionTeam_);
                    if (hiddenStand == null){
                        break;
                    }

                    hiddenStands.put(hiddenStand.getUUIDSlime(), hiddenStand);
                } else if (obj instanceof MemorySection memorySection){
                    HiddenStand hiddenStand = HiddenStand.deserialize(memorySection.getValues(false), noCollisionTeam_);
                    if (hiddenStand == null){
                        break;
                    }
                    hiddenStands.put(hiddenStand.getUUIDSlime(), hiddenStand);
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize hidden stand: " + obj + ", skipping. Reason: unknown type.");
                }
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize uuid list: " + temp);
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
                            GreenLogger.log(Level.WARNING, "couldn't deserialize head item property: " + obj2 + ", skipping. Reason: not a string.");
                        }
                    }
                    heads.add(ItemStack.deserialize(itemStackMap));
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize head item: " + obj + ", skipping. Reason: not a item stack nor map.");
                }
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize head list: " + temp);
            return null;
        }

        temp = data.get(NAME_KEY);
        String name;
        if (temp instanceof String tempName){
            name = tempName;
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize name: " + ". Reason: is not a string.");
            return null;
        }

        temp = data.get(ALLOW_LATE_JOIN_KEY);
        Boolean allowLateJoin;
        if (temp instanceof Boolean tempBool){
            allowLateJoin = tempBool;
        } else if (temp instanceof String str){
            allowLateJoin = BooleanUtils.toBooleanObject(str);

            if (allowLateJoin == null){
                GreenLogger.log(Level.SEVERE, "couldn't deserialize allowLateJoin: " + str + ". Reason: string is not a bool.");
                return null;
            }
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize allowLateJoin bool: " + temp);
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
                    GreenLogger.log(Level.WARNING, "couldn't deserialize lobby location property: " + obj + ", skipping. Reason: not a string.");
                }
            }
            lobbyLoc = Location.deserialize(stringObjectMap);
        } else if (temp instanceof MemorySection memorySection) {
            lobbyLoc = Location.deserialize(memorySection.getValues(false));
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize lobby location: " + temp);
        }

        temp = data.get(START_LOC_KEY);
        Location startLoc = null;
        if (temp instanceof Map<?, ?> map2){
            Map<String, Object> stringObjectMap = new HashMap<>();

            for (Object obj: map2.keySet()){
                if (obj instanceof String str){
                    stringObjectMap.put(str, map2.get(obj));
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize start location property: " + obj + ", skipping. Reason: not a string.");
                }
            }
            startLoc = Location.deserialize(stringObjectMap);
        } else if (temp instanceof MemorySection memorySection) {
            startLoc = Location.deserialize(memorySection.getValues(false));
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize start location: " + temp);
        }

        temp = data.get(QUIT_LOC_KEY);
        Location quitLoc = null;
        if (temp instanceof Map<?, ?> map3){
            Map<String, Object> stringObjectMap = new HashMap<>();

            for (Object obj: map3.keySet()){
                if (obj instanceof String str){
                    stringObjectMap.put(str, map3.get(obj));
                } else {
                    GreenLogger.log(Level.WARNING, "couldn't deserialize quit property: " + obj + ", skipping. Reason: not a string.");
                }
            }
            quitLoc = Location.deserialize(stringObjectMap);
        } else if (temp instanceof MemorySection memorySection) {
            quitLoc = Location.deserialize(memorySection.getValues(false));
        } else {
            GreenLogger.log(Level.WARNING, "couldn't deserialize quit location: " + temp);
        }

        temp = data.get(GAME_TIME_LENGTH_KEY);
        long game_time_length;
        if (temp instanceof Long tempLong){
            game_time_length = tempLong;
        } else if(temp instanceof String str){
            if (Utils.islong(str)){
                game_time_length = Long.parseLong(str);
            } else {
                GreenLogger.log(Level.SEVERE, "couldn't deserialize game time length long: " + str + " not a long! (1)");
                return null;
            }
        } else if (temp instanceof Integer tempLong){
            game_time_length = tempLong;
        } else {
            GreenLogger.log(Level.SEVERE, "couldn't deserialize game time length long: " + temp + " not a long! (2)");
            return null;
        }

        return new Game(scoreboard_, noCollisionTeam_, starting_hidden_percent,
                average_ticks_until_rehead, min_milli_rehead_cooldown,
                hiddenStands, heads,
                name,
                allowLateJoin,
                lobbyLoc, startLoc, quitLoc,
                game_time_length);

    }

    /**
     * creates a new armor stand at the given location and keeps track of it (saves the game)
     * @param location
     */
    public void addHiddenStand(@NotNull Location location){
        HiddenStand hiddenStand = new HiddenStand(location, name, noCollisionTeam);
        hiddenStands.put(hiddenStand.getUUIDSlime(), hiddenStand);

        MainConfig.inst().saveGame(this);
    }

    /**
     * gives all hidden stands around the given location a glowing effect for 10s
     * @param location location around all hidden stands will get the glowing effect
     * @param range range how far will be searched for hidden stands
     */
    public void showAroundLocation(@NotNull Location location, int range){
        World world = location.getWorld();

        for (HiddenStand hiddenStand : hiddenStands.values()){
            if (hiddenStand.getSlime() != null){
                Location standLoc = hiddenStand.getSlime().getLocation();

                if (world != standLoc.getWorld())
                    continue;

                if (NumberConversions.square(range) >= (NumberConversions.square(location.getX() - standLoc.getX()) +
                        NumberConversions.square(location.getY() - standLoc.getY()) +
                        NumberConversions.square(location.getZ() - standLoc.getZ()))){
                    hiddenStand.getSlime().setGlowing(true);

                    Bukkit.getScheduler().runTaskLater(FindMe.inst(), () ->
                            hiddenStand.getSlime().setGlowing(false), 200);
                }
            }
        }
    }

    /**
     * get the nearest hidden armor stand in a range of 5 blocks
     * @param startingPos location around the nearest stand will be searched
     * @return nearest hidden armor stand or null if no where found
     */
    public @Nullable HiddenStand getNearestStand(@NotNull Location startingPos){
        HiddenStand result = null;
        double lastDistance = Double.MAX_VALUE;

        Collection<Slime> nearbySlimes = startingPos.getNearbyEntitiesByType(Slime.class, 5);
        Set<HiddenStand> nearbyStands = nearbySlimes.stream().
                //get all tracked slimes that are in radius
                map(s -> hiddenStands.get(s.getUniqueId())).filter(Objects::nonNull).
                collect(Collectors.toSet());

        for(HiddenStand hiddenStand : nearbyStands) {
            //we don't need the root, if we only want to know what entity is further
            //also, hiddenStand.getSlime is backed up by getting the initial slimes from getNearbyEntitiesByType,
            //so it never will be null.
            double distance = startingPos.distanceSquared(hiddenStand.getSlime().getLocation());
            if(distance < lastDistance) {
                lastDistance = distance;
                result = hiddenStand;
            }
        }

        return result;
    }

    /**
     * removes a hidden armor stand + slime pair and no longer keeps track of it (saves the game)
     * @param uuid of a slime the armor stand belongs to
     */
    public void removeHiddenStand(@NotNull UUID uuid){
        HiddenStand hiddenStand =  hiddenStands.get(uuid);
        //kill the entities
        if (hiddenStand != null){
            Slime slime = hiddenStand.getSlime();
            if (slime != null){
                slime.setHealth(0);
            }

            ArmorStand armorStand = hiddenStand.getArmorStand();
            if (armorStand != null){
                armorStand.setHealth(0);
            }
        }

        hiddenStands.remove(uuid);

        MainConfig.inst().saveGame(this);
    }

    /**
     * forces the game to end and clears up the tracked data.
     * Will get the game into a broken state and is only for shutting doen
     */
    protected void clear(){
        end();

        hiddenStands.clear();
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
            noCollisionTeam.addPlayer(player);

            if (startLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            } else {
                GreenLogger.log(Level.WARNING, "No start postion was given for FindMe! game \"" + name + "\". Couldn't teleport anybody.");
            }
        } else {
            if (lobbyLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
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
        noCollisionTeam.removePlayer(player);

        if (quitLoc != null){
            if (Utils.isPaper()){
                player.teleportAsync(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                player.teleport(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
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
     * shows the remaining time and handles reheading the hidden armor stands
     * after the time runs out it will end the game.
     */
    private void GameTimer(){
        remainingGameTime--;

        TimeHelper timeHelper = new TimeHelper(remainingGameTime);
        boolean shouldMakeCountdownNoise = (remainingGameTime <= TimeHelper.TICKS_PER_SECOND * 10) && (remainingGameTime % 20 == 0);

        for (Player player : players){
            //show remaining game time
            player.sendActionBar(Lang.build(timeHelper.getMinutes() + " : " + timeHelper.getSeconds()));

            //end countdown
            if (shouldMakeCountdownNoise){
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.8f, 1.0f);
            }
        }

        //rehead hidden stands, if it is not in cooldown
        final int max = heads.size();

        //only try to rehead if we have heads to begin with
        Random random = new Random();
        ItemStack[] headArray = heads.toArray(new ItemStack[0]);

        for (HiddenStand hiddenStand : hiddenStands.values()){
            if (hiddenStand.getArmorStand() != null) {
                ItemStack headItemstack = hiddenStand.getArmorStand().getEquipment().getHelmet();

                if ((headItemstack == null || headItemstack.getType().isAir()) &&
                        hiddenStand.isCooldownOver(minMillisUntilRehead) &&
                        random.nextLong(averageTicksUntilRehead) == 1){
                    hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, headArray[max == 0 ? 0 : random.nextInt(max)]);
                    hiddenStand.setCooldownNow();
                }
            }
        }

        //next game tick or end of game
        if (remainingGameTime >= 0){
            Bukkit.getScheduler().runTaskLater(FindMe.inst(), this::GameTimer, 1);
        } else {
            end();
        }
    }

    /**
     * start of the main phase of the game.
     * set random heads for the configurated percentage of hidden stands and show scoreboard;
     * teleport all players to starting location if possible, and start main loop
     */
    private void startMain (){
        gameState = GameStates.ACTIVE;
        final int max = heads.size();
        //only set heads if the set isn't empty
        Random random = new Random();
        ItemStack[] headArray = heads.toArray(new ItemStack[0]);

        for (HiddenStand hiddenStand : hiddenStands.values()) {
            if (hiddenStand.getArmorStand() != null){
                if (random.nextInt(100) <= startingHiddenPercent) {
                    hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, headArray[max == 0 ? 0 : random.nextInt(max)]);
                } else {
                    hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR, 1));
                }
            }

            hiddenStand.setCooldownNow();
        }

        for (Player player : players){
            player.setScoreboard(scoreboard);
            noCollisionTeam.addPlayer(player);

            if (startLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(startLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }

        if (startLoc == null){
            GreenLogger.log(Level.WARNING, "No start postion was given for FindMe! game \"" + name + "\". Couldn't teleport anybody.");
        }

        remainingGameTime = gameTimeLength;
        Bukkit.getScheduler().runTaskLater(FindMe.inst(), this::GameTimer, 1);
    }

    /**
     * called whenever a player finds a hidden stand (technically the slime of it), increments the score of the player
     * @param player the player who found the stand
     * @param uuid uuid of the slime part of a hidden stand
     */
    public void findStand(@NotNull Player player, @NotNull UUID uuid){
        HiddenStand hiddenStand = hiddenStands.get(uuid);

        if (hiddenStand == null || hiddenStand.getArmorStand() == null){
            return;
        }

        //don't react to empty stands
        ItemStack oldHelmet = hiddenStand.getArmorStand().getEquipment().getHelmet();
        if (oldHelmet == null || oldHelmet.getType().isAir()){
            return;
        }

        //remove head & play sound
        hiddenStand.getArmorStand().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR, 1));
        hiddenStand.getArmorStand().getLocation().getWorld().playSound(hiddenStand.getArmorStand(), Sound.ENTITY_ITEM_PICKUP,0.9f, 0.9f);

        addScore(player, 1);
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
        for (Player player : players){
            scoreboard.resetScores(player);
            noCollisionTeam.removePlayer(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

            if (quitLoc != null){
                if (Utils.isPaper()){
                    player.teleportAsync(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(quitLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
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
     * set how many ticks pass on average until a hidden armor stand gets its head back
     * @param ticks
     */
    public void setAverageTicksUntilRehead(long ticks) {
        this.averageTicksUntilRehead = ticks;
    }

    /**
     * get how many ticks pass on average until a hidden armor stand gets its head back
     * @return
     */
    public long getAverageTicksUntilRehead(){
        return this.averageTicksUntilRehead;
    }

    /**
     * set how much percent of all hidden stands will have a head when the game starts
     * @param percent 0 - 100, all values above 100 will act the same as 100 and all values under 0 will be the same as o
     */
    public void setStartingHiddenPercent(double percent) {
        this.startingHiddenPercent = percent;
    }

    /**
     * gets how much percent of all hidden stands will have a head when the game starts
     * @return
     */
    public double getStartingHiddenPercent(){
        return this.startingHiddenPercent;
    }

    /**
     * set how long a hidden stand can not get its head back, after it was found
     * @param millis
     */
    public void setReheadCooldown(long millis){
        this.minMillisUntilRehead = millis;
    }

    /**
     * get how long a hidden stand can not get its head back, after it was found
     * @return
     */
    public long getReheadCooldown(){
        return this.minMillisUntilRehead;
    }

    /**
     * get how long the active game will still be running for. will be 0, if it isn't running.
     * @return
     */
    public long getRemainingGameTime(){
        return remainingGameTime;
    }
}
