package cn.nukkit;

import cn.nukkit.block.Block;
import cn.nukkit.blockentity.*;
import cn.nukkit.command.*;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.item.*;
import cn.nukkit.entity.mob.EntityCreeper;
import cn.nukkit.entity.passive.*;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.entity.weather.EntityLightning;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.TextContainer;
import cn.nukkit.event.TranslationContainer;
import cn.nukkit.event.level.LevelInitEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.server.QueryRegenerateEvent;
import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.BaseLang;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.LevelProviderManager;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.format.leveldb.LevelDB;
import cn.nukkit.level.format.mcregion.McRegion;
import cn.nukkit.level.generator.Flat;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.Hell;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.metadata.EntityMetadataStore;
import cn.nukkit.metadata.LevelMetadataStore;
import cn.nukkit.metadata.PlayerMetadataStore;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.CompressBatchedTask;
import cn.nukkit.network.Network;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerListPacket;
import cn.nukkit.network.query.QueryHandler;
import cn.nukkit.permission.DefaultPermissions;
import cn.nukkit.permission.Permissible;
import cn.nukkit.plugin.JavaPluginLoader;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginLoadOrder;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import cn.nukkit.scheduler.FileWriteTask;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.*;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * author: MagicDroidX & Box
 * Nukkit
 */
public class Server {

    public static final String BROADCAST_CHANNEL_ADMINISTRATIVE = "nukkit.broadcast.admin";
    public static final String BROADCAST_CHANNEL_USERS = "nukkit.broadcast.user";

    public static Server instance = null;
    public boolean isRunning = true;

    public boolean hasStopped = false;

    public PluginManager pluginManager = null;

    public int profilingTickrate = 20;

    public ServerScheduler scheduler = null;

    public int tickCounter;

    public long nextTick;

    public float[] tickAverage = {20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20};

    public float[] useAverage = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public float maxTick = 20;

    public float maxUse = 0;

    public int sendUsageTicker = 0;

    public boolean dispatchSignals = false;

    public MainLogger logger;

    public CommandReader console;

    public SimpleCommandMap commandMap;

    public CraftingManager craftingManager;

    public ConsoleCommandSender consoleSender;

    public EntityMetadataStore entityMetadata;

    public PlayerMetadataStore playerMetadata;

    public LevelMetadataStore levelMetadata;

    public Network network;

    public int autoSaveTicker = 0;
    public int autoSaveTicks = 6000;

    public BaseLang baseLang;

    public boolean forceLanguage;

    public UUID serverID;

    public String filePath;
    public String dataPath;
    public String pluginPath;

    public Set<UUID> uniquePlayers = new HashSet<>();

    public QueryHandler queryHandler;

    public QueryRegenerateEvent queryRegenerateEvent;

    public Map<String, Player> players = new HashMap<>();

    public Map<UUID, Player> playerList = new HashMap<>();

    public Map<Integer, String> identifier = new HashMap<>();

    public Map<Integer, Level> levels = new HashMap<>();

    public Level defaultLevel = null;
    public Level netherLevel = null;

    public Server(MainLogger logger, final String filePath, String dataPath, String pluginPath) {
        instance = this;
        this.logger = logger;

        this.filePath = filePath;
        if (!new File(dataPath + "worlds/").exists()) {
            new File(dataPath + "worlds/").mkdirs();
        }

        if (!new File(dataPath + "players/").exists()) {
            new File(dataPath + "players/").mkdirs();
        }

        if (!new File(pluginPath).exists()) {
            new File(pluginPath).mkdirs();
        }

        this.dataPath = new File(dataPath).getAbsolutePath() + "/";
        this.pluginPath = new File(pluginPath).getAbsolutePath() + "/";

        console = new CommandReader();
        //todo: VersionString 现在不必要


        if (!new File(dataPath + "server.properties").exists()) {
            logger.info(TextFormat.GREEN + "Welcome! Please choose a language first!");
            try {
                String[] lines = Utils.readFile(getClass().getClassLoader().getResourceAsStream("lang/language.list")).split("\n");
                for (String line : lines) {
                    logger.info(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String language = null;
            while (language == null) {
                String lang = console.readLine();
                InputStream conf = getClass().getClassLoader().getResourceAsStream("lang/" + lang + "/lang.ini");
                if (conf != null) {
                    language = lang;
                }
            }
        }
        logger.info("Loading " + TextFormat.GREEN + "server properties" + TextFormat.WHITE + "...");
        loadConfig();

        console.start();
        baseLang = new BaseLang(ServerProperties.language);
        logger.info(getLanguage().translateString("language.selected", new String[]{getLanguage().getName(), getLanguage().getLang()}));
        logger.info(getLanguage().translateString("nukkit.server.start", TextFormat.AQUA + getVersion() + TextFormat.WHITE));

        ServerScheduler.WORKERS = ServerProperties.async_workers == -1 ? Math.max(Runtime.getRuntime().availableProcessors() + 1, 4) : ServerProperties.async_workers;

        Network.BATCH_THRESHOLD = ServerProperties.network_batch_threshold < 0 ? -1 : ServerProperties.network_batch_threshold;

        scheduler = new ServerScheduler();

        entityMetadata = new EntityMetadataStore();
        playerMetadata = new PlayerMetadataStore();
        levelMetadata = new LevelMetadataStore();

        Field[] f = ServerInfo.class.getFields();
        for (Field g : f) {
            if (g.getType() == ServerInfo.class) {
                ServerInfo.fields.put(g.getName(), g);
                try {
                    ((ServerInfo<String>) g.get(null)).infoName = g.getName();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        loadInfo();

        if (ServerProperties.hardcore && ServerProperties.difficulty < 3)
            ServerProperties.difficulty = 3;


        Nukkit.DEBUG = ServerProperties.debug_level;
        logger.setLogDebug(Nukkit.DEBUG > 1);

        logger.info(getLanguage().translateString("nukkit.server.networkStart", new String[]{ServerProperties.server_ip.isEmpty() ? "*" : ServerProperties.server_ip, String.valueOf(ServerProperties.server_port)}));
        serverID = UUID.randomUUID();

        network = new Network(this);
        network.setName(ServerProperties.motd);

        logger.info(getLanguage().translateString("nukkit.server.info", new String[]{getName(), TextFormat.YELLOW + getNukkitVersion() + TextFormat.WHITE, TextFormat.AQUA + getCodename() + TextFormat.WHITE, getApiVersion()}));
        logger.info(getLanguage().translateString("nukkit.server.license", getName()));

        consoleSender = new ConsoleCommandSender();
        commandMap = new SimpleCommandMap(this);

        registerEntities();
        registerBlockEntities();

        InventoryType.init();
        Block.init();
        Item.init();
        Biome.init();
        Effect.init();
        Potion.init();
        Enchantment.init();
        Attribute.init();

        craftingManager = new CraftingManager();

        pluginManager = new PluginManager(this, commandMap);
        pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, consoleSender);

        pluginManager.registerInterface(JavaPluginLoader.class);

        queryRegenerateEvent = new QueryRegenerateEvent(this, 5);

        network.registerInterface(new RakNetInterface(this));

        pluginManager.loadPlugins(pluginPath);

        enablePlugins(PluginLoadOrder.STARTUP);

        LevelProviderManager.addProvider(this, Anvil.class);
        LevelProviderManager.addProvider(this, McRegion.class);
        LevelProviderManager.addProvider(this, LevelDB.class);

        Generator.addGenerator(Flat.class, "flat", Generator.TYPE_FLAT);
        Generator.addGenerator(Normal.class, "default", Generator.TYPE_INFINITE);
        Generator.addGenerator(Hell.class, "nether", Generator.TYPE_NETHER);
/*
        for (String name : ((Map<String, Object>) getConfig("worlds", new HashMap<>())).keySet()) {
            if (!loadLevel(name)) {
                long seed;
                try {
                    seed = ((Integer) getConfig("worlds." + name + ".seed")).longValue();
                } catch (Exception e) {
                    seed = System.currentTimeMillis();
                }

                Map<String, Object> options = new HashMap<>();
                String[] opts = ((String) getConfig("worlds." + name + ".generator", Generator.getGenerator("default").getSimpleName())).split(":");
                Class<? extends Generator> generator = Generator.getGenerator(opts[0]);
                if (opts.length > 1) {
                    StringBuilder preset = new StringBuilder();
                    for (int i = 1; i < opts.length; i++)
                        preset.append(opts[i]).append(":");
                    preset = new StringBuilder(preset.substring(0, preset.length() - 1));

                    options.put("preset", preset.toString());
                }

                generateLevel(name, seed, generator, options);
            }
        }
*/
        long seed = 0;
        if (!ServerProperties.level_seed.isEmpty()) try {
            seed = Long.parseLong(ServerProperties.level_seed);
        } catch (NumberFormatException e) {
            seed = ServerProperties.level_seed.hashCode();
        }

        if (getDefaultLevel() == null) {
            String defaultName;
            if (ServerProperties.level_name.isEmpty()) {
                logger.warning("level-name cannot be null, using default");
                ServerProperties.level_name = defaultName = "world";
            } else
                defaultName = ServerProperties.level_name;
            if (!loadLevel(defaultName))
                generateLevel(defaultName, seed == 0 ? System.currentTimeMillis() : seed);
            setDefaultLevel(getLevelByName(defaultName));
        }

        if (ServerProperties.nether_enabled) {
            if (!loadLevel(ServerProperties.nether_name))
                generateLevel(ServerProperties.nether_name, seed == 0 ? System.currentTimeMillis() : seed, Generator.getGenerator("nether"));
            netherLevel = getLevelByName(ServerProperties.nether_name);
        }
        saveConfig(true);

        if (getDefaultLevel() == null) {
            logger.emergency(getLanguage().translateString("nukkit.level.defaultError"));
            forceShutdown();

            return;
        }

        enablePlugins(PluginLoadOrder.POSTWORLD);

        queryRegenerateEvent = new QueryRegenerateEvent(this, 5);

        start();
    }

    public void loadConfig(String path, Class<?> cls) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("Could not create Config " + file, e);
            }
            saveConfig(false);
        } else {
            Field[] fields = cls.getFields();
            HashMap<String, Field> map = new HashMap<>();
            for (Field field : fields)
                map.put(field.getName(), field);
            try {
                String ct = Utils.readFile(file);
                for (String line : ct.split("\n")) {
                    if (Pattern.compile("[a-zA-Z0-9\\-_\\.]*+=+[^\\r\\n]*").matcher(line).matches()) {
                        String[] b = line.split("=", -1);
                        String k = b[0];
                        String v = b[1].trim();
                        Field f;
                        if ((f = map.get(k)) != null) {
                            Class<?> c = f.getType();
                            if (c == int.class) f.set(null, Integer.valueOf(v));
                            else if (c == String.class) f.set(null, v);
                            else if (c == boolean.class) f.set(null, Boolean.valueOf(v));
                        }
                    }
                }
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadConfig() {
        loadConfig(dataPath + "server.properties", ServerProperties.class);
    }

    public void saveConfig(String path, Class<?> cls, boolean async) {
        File file = new File(path);
        Field[] fields = cls.getFields();
        StringBuilder ct = new StringBuilder();
        try {
            for (Field field : fields)
                ct.append(field.getName()).append("=").append(field.get(null)).append("\n");
            if (async) Server.getInstance().getScheduler().scheduleAsyncTask(new FileWriteTask(file, ct.toString()));
            else Utils.writeFile(file, ct.toString());
        } catch (IllegalAccessException | IOException e) {
            logger.logException(e);
        }
    }

    public void saveConfig(boolean async) {
        saveConfig(dataPath + "server.properties", ServerProperties.class, async);
    }

    public void loadInfo() {
        Field[] fields = ServerInfo.class.getFields();
        for (Field field : fields)
            if (field.getType() == ServerInfo.class)
                loadInfo(field);
    }

    public void loadInfo(Field field) {
        File file = new File(dataPath + field.getName() + ".txt");
        if (!file.exists()) try {
            file.createNewFile();
        } catch (IOException e) {
            logger.error("Could not create Info " + file, e);
        }
        else {
            try {
                ServerInfo<String> info = (ServerInfo<String>) field.get(null);
                String ct = Utils.readFile(file);
                for (String line : ct.split("\n")) {
                    line = line.trim();
                    if (!line.isEmpty())
                        info.add(line);
                }
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadInfo(String field) {
        loadInfo(ServerInfo.fields.get(field));
    }

    public void saveInfo(boolean async) {
        Field[] fields = ServerInfo.class.getFields();
        for (Field field : fields)
            if (field.getType() == ServerInfo.class)
                saveInfo(field, async);
    }

    public void saveInfo(String field, boolean async) {
        saveInfo(ServerInfo.fields.get(field), async);
    }

    public void saveInfo(Field field, boolean async) {
        File file = new File(dataPath + field.getName() + ".txt");
        StringBuilder ct = new StringBuilder();
        try {
            ServerInfo<String> info = (ServerInfo<String>) field.get(null);
            for (String s : info) ct.append(s).append("\n");
            if (async)
                Server.getInstance().getScheduler().scheduleAsyncTask(new FileWriteTask(file, ct.toString()));
            else Utils.writeFile(file, ct.toString());
        } catch (IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int broadcastMessage(String message) {
        return broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    public int broadcastMessage(TextContainer message) {
        return broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    public int broadcastMessage(String message, CommandSender[] recipients) {
        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.length;
    }

    public int broadcastMessage(String message, Collection<CommandSender> recipients) {
        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    public int broadcastMessage(TextContainer message, Collection<CommandSender> recipients) {
        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    public int broadcast(String message, String permissions) {
        Set<CommandSender> recipients = new HashSet<>();

        for (String permission : permissions.split(";")) {
            for (Permissible permissible : pluginManager.getPermissionSubscriptions(permission)) {
                if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                    recipients.add((CommandSender) permissible);
                }
            }
        }

        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    public int broadcast(TextContainer message, String permissions) {
        Set<CommandSender> recipients = new HashSet<>();

        for (String permission : permissions.split(";")) {
            for (Permissible permissible : pluginManager.getPermissionSubscriptions(permission)) {
                if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                    recipients.add((CommandSender) permissible);
                }
            }
        }

        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }


    public static void broadcastPacket(Collection<Player> players, DataPacket packet) {
        broadcastPacket(players.toArray(new Player[0]), packet);
    }

    public static void broadcastPacket(Player[] players, DataPacket packet) {
        packet.encode();
        packet.isEncoded = true;
        if (Network.BATCH_THRESHOLD >= 0 && packet.getBuffer().length >= Network.BATCH_THRESHOLD) {
            Server.getInstance().batchPackets(players, new DataPacket[]{packet}, false);
            return;
        }

        for (Player player : players) {
            player.dataPacket(packet);
        }

        if (packet.encapsulatedPacket != null) {
            packet.encapsulatedPacket = null;
        }
    }

    public void batchPackets(Player[] players, DataPacket[] packets) {
        batchPackets(players, packets, false);
    }

    public void batchPackets(Player[] players, DataPacket[] packets, boolean forceSync) {
        if (players == null || packets == null || players.length == 0 || packets.length == 0) {
            return;
        }

        byte[][] payload = new byte[packets.length * 2][];
        for (int i = 0; i < packets.length; i++) {
            DataPacket p = packets[i];
            if (!p.isEncoded) {
                p.encode();
            }
            byte[] buf = p.getBuffer();
            payload[i * 2] = Binary.writeInt(buf.length);
            payload[i * 2 + 1] = buf;
        }
        byte[] data;
        data = Binary.appendBytes(payload);

        List<String> targets = new ArrayList<>();
        for (Player p : players) {
            if (p.isConnected()) {
                targets.add(identifier.get(p.rawHashCode()));
            }
        }

        if (!forceSync && ServerProperties.network_compression_async) {
            getScheduler().scheduleAsyncTask(new CompressBatchedTask(data, targets, ServerProperties.network_compression_level));
        } else {
            try {
                broadcastPacketsCallback(Zlib.deflate(data, ServerProperties.network_compression_level), targets);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void broadcastPacketsCallback(byte[] data, List<String> identifiers) {
        BatchPacket pk = new BatchPacket();
        pk.payload = data;
        pk.encode();
        pk.isEncoded = true;

        for (String i : identifiers) {
            if (players.containsKey(i)) {
                players.get(i).dataPacket(pk);
            }
        }
    }

    public void enablePlugins(PluginLoadOrder type) {
        for (Plugin plugin : pluginManager.getPlugins().values()) {
            if (!plugin.isEnabled() && type == plugin.getDescription().getOrder()) {
                enablePlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            commandMap.registerServerAliases();
            DefaultPermissions.registerCorePermissions();
        }
    }

    public void enablePlugin(Plugin plugin) {
        pluginManager.enablePlugin(plugin);
    }

    public void disablePlugins() {
        pluginManager.disablePlugins();
    }

    public boolean dispatchCommand(CommandSender sender, String commandLine) throws ServerException {
        if (sender == null) {
            throw new ServerException("CommandSender is not valid");
        }

        if (commandMap.dispatch(sender, commandLine)) {
            return true;
        }

        sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.notFound"));

        return false;
    }

    //todo: use ticker to check console
    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }

    public void reload() {
        logger.info("Reloading...");

        logger.info("Saving levels...");

        for (Level level : levels.values()) {
            level.save();
        }

        pluginManager.disablePlugins();
        pluginManager.clearPlugins();
        commandMap.clearCommands();

        logger.info("Reloading properties...");
        loadConfig();

        if (ServerProperties.hardcore && ServerProperties.difficulty < 3) {
            ServerProperties.difficulty = 3;
        }

        loadInfo();

        for (String ip : ServerInfo.banByIP) {
            getNetwork().blockAddress(ip, -1);
        }

        pluginManager.registerInterface(JavaPluginLoader.class);
        pluginManager.loadPlugins(pluginPath);
        enablePlugins(PluginLoadOrder.STARTUP);
        enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    public void shutdown() {
        if (isRunning) {
            ServerKiller killer = new ServerKiller(90);
            killer.start();
        }
        isRunning = false;
    }

    public void forceShutdown() {
        if (hasStopped) {
            return;
        }

        try {
            if (!isRunning) {
                //todo sendUsage
            }

            // clean shutdown of console thread asap
            console.shutdown();

            hasStopped = true;

            shutdown();

            logger.debug("Disabling all plugins");
            pluginManager.disablePlugins();

            for (Player player : new ArrayList<>(players.values())) {
                player.close(player.getLeaveMessage(), ServerProperties.shutdown_message);
            }

            logger.debug("Unloading all levels");
            for (Level level : new ArrayList<>(getLevels().values())) {
                unloadLevel(level, true);
            }

            logger.debug("Removing event handlers");
            HandlerList.unregisterAll();

            logger.debug("Stopping all tasks");
            scheduler.cancelAllTasks();
            scheduler.mainThreadHeartbeat(Integer.MAX_VALUE);

            logger.debug("Saving properties");
            saveConfig(true);
            saveInfo(true);

            logger.debug("Closing console");
            console.interrupt();

            logger.debug("Stopping network interfaces");
            for (SourceInterface interfaz : network.getInterfaces()) {
                interfaz.shutdown();
                network.unregisterInterface(interfaz);
            }

            //todo other things
        } catch (Exception e) {
            logger.logException(e); //todo remove this?
            logger.emergency("Exception happened while shutting down, exit the process");
            System.exit(1);
        }
    }

    public void start() {
        if (ServerProperties.enable_query)
            queryHandler = new QueryHandler();

        for (String ip : ServerInfo.banByIP) {
            getNetwork().blockAddress(ip, -1);
        }

        //todo send usage setting

        tickCounter = 0;

        logger.info(getLanguage().translateString("nukkit.server.defaultGameMode", getGamemodeString(ServerProperties.gamemode)));

        logger.info(getLanguage().translateString("nukkit.server.startFinished", String.valueOf((double) (System.currentTimeMillis() - Nukkit.START_TIME) / 1000)));

        tickProcessor();
        forceShutdown();
    }

    public void handlePacket(String address, int port, byte[] payload) {
        try {
            if (payload.length > 2 && Arrays.equals(Binary.subBytes(payload, 0, 2), new byte[]{(byte) 0xfe, (byte) 0xfd}) && queryHandler != null) {
                queryHandler.handle(address, port, payload);
            }
        } catch (Exception e) {
            logger.logException(e);

            getNetwork().blockAddress(address, 600);
        }
    }

    public void tickProcessor() {
        nextTick = System.currentTimeMillis();
        while (isRunning) {
            try {
                tick();
            } catch (RuntimeException e) {
                logger.logException(e);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Server.getInstance().logger.logException(e);
            }
        }
    }

    public void onPlayerLogin(Player player) {
        if (sendUsageTicker > 0) {
            uniquePlayers.add(player.getUniqueId());
        }
    }

    public void addPlayer(String identifier, Player player) {
        players.put(identifier, player);
        this.identifier.put(player.rawHashCode(), identifier);
    }

    public void addOnlinePlayer(Player player) {
        addOnlinePlayer(player, true);
    }

    public void addOnlinePlayer(Player player, boolean update) {
        playerList.put(player.getUniqueId(), player);

        if (update) {
            updatePlayerListData(player.getUniqueId(), player.getId(), player.getDisplayName(), player.getSkin());
        }
    }

    public void removeOnlinePlayer(Player player) {
        if (playerList.containsKey(player.getUniqueId())) {
            playerList.remove(player.getUniqueId());

            PlayerListPacket pk = new PlayerListPacket();
            pk.type = PlayerListPacket.TYPE_REMOVE;
            pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(player.getUniqueId())};

            Server.broadcastPacket(playerList.values(), pk);
        }
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin) {
        updatePlayerListData(uuid, entityId, name, skin, playerList.values());
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin, Player[] players) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_ADD;
        pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(uuid, entityId, name, skin)};
        Server.broadcastPacket(players, pk);
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin, Collection<Player> players) {
        updatePlayerListData(uuid, entityId, name, skin, players.stream().toArray(Player[]::new));
    }

    public void removePlayerListData(UUID uuid) {
        removePlayerListData(uuid, playerList.values());
    }

    public void removePlayerListData(UUID uuid, Player[] players) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_REMOVE;
        pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(uuid)};
        Server.broadcastPacket(players, pk);
    }

    public void removePlayerListData(UUID uuid, Collection<Player> players) {
        removePlayerListData(uuid, players.stream().toArray(Player[]::new));
    }

    public void sendFullPlayerListData(Player player) {
        sendFullPlayerListData(player, false);
    }

    public void sendFullPlayerListData(Player player, boolean self) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_ADD;
        List<PlayerListPacket.Entry> entries = new ArrayList<>();
        for (Player p : playerList.values()) {
            if (!self && p == player) {
                continue;
            }

            entries.add(new PlayerListPacket.Entry(p.getUniqueId(), p.getId(), p.getDisplayName(), p.getSkin()));
        }

        pk.entries = entries.stream().toArray(PlayerListPacket.Entry[]::new);

        player.dataPacket(pk);
    }

    public void sendRecipeList(Player player) {
        CraftingDataPacket pk = new CraftingDataPacket();
        pk.cleanRecipes = true;

        for (Recipe recipe : getCraftingManager().getRecipes().values()) {
            if (recipe instanceof ShapedRecipe) {
                pk.addShapedRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                pk.addShapelessRecipe((ShapelessRecipe) recipe);
            }
        }

        for (FurnaceRecipe recipe : getCraftingManager().getFurnaceRecipes().values()) {
            pk.addFurnaceRecipe(recipe);
        }

        player.dataPacket(pk);
    }

    public void checkTickUpdates(int currentTick, long tickTime) {
        for (Player p : new ArrayList<>(players.values())) {
            if (!p.loggedIn && (tickTime - p.creationTime) >= 10000) 
                p.close("", "Login timeout");
            else if (ServerProperties.always_tick_players)
                p.onUpdate(currentTick);
        }

        //Do level ticks
        for (Level level : getLevels().values()) {
            if (level.getTickRate() > ServerProperties.base_tick_rate && --level.tickRateCounter > 0) {
                continue;
            }

            try {
                long levelTime = System.currentTimeMillis();
                level.doTick(currentTick);
                int tickMs = (int) (System.currentTimeMillis() - levelTime);
                level.tickRateTime = tickMs;

                if (ServerProperties.auto_tick_rate) {
                    if (tickMs < 50 && level.getTickRate() > ServerProperties.base_tick_rate) {
                        int r;
                        level.setTickRate(r = level.getTickRate() - 1);
                        if (r > ServerProperties.base_tick_rate) {
                            level.tickRateCounter = level.getTickRate();
                        }
                        logger.debug("Raising level \"" + level.getName() + "\" tick rate to " + level.getTickRate() + " ticks");
                    } else if (tickMs >= 50) {
                        if (level.getTickRate() == ServerProperties.base_tick_rate) {
                            level.setTickRate((int) Math.max(ServerProperties.base_tick_rate + 1, Math.min(ServerProperties.auto_tick_rate_limit, Math.floor(tickMs / 50))));
                            logger.debug("Level \"" + level.getName() + "\" took " + NukkitMath.round(tickMs, 2) + "ms, setting tick rate to " + level.getTickRate() + " ticks");
                        } else if ((tickMs / level.getTickRate()) >= 50 && level.getTickRate() < ServerProperties.auto_tick_rate_limit) {
                            level.setTickRate(level.getTickRate() + 1);
                            logger.debug("Level \"" + level.getName() + "\" took " + NukkitMath.round(tickMs, 2) + "ms, setting tick rate to " + level.getTickRate() + " ticks");
                        }
                        level.tickRateCounter = level.getTickRate();
                    }
                }
            } catch (Exception e) {
                if (Nukkit.DEBUG > 1 && logger != null) {
                    logger.logException(e);
                }

                logger.critical(getLanguage().translateString("nukkit.level.tickError", new String[]{level.getName(), e.toString()}));
            }
        }
    }

    public void doAutoSave() {
        if (ServerProperties.auto_save) {
            for (Player player : new ArrayList<>(players.values())) {
                if (player.isOnline()) {
                    player.save(true);
                } else if (!player.isConnected()) {
                    removePlayer(player);
                }
            }

            for (Level level : getLevels().values()) {
                level.save();
            }
        }
    }

    public boolean tick() {
        long tickTime = System.currentTimeMillis();
        long tickTimeNano = System.nanoTime();
        if ((tickTime - nextTick) < -25) {
            return false;
        }

        ++tickCounter;

        network.processInterfaces();

        scheduler.mainThreadHeartbeat(tickCounter);

        checkTickUpdates(tickCounter, tickTime);

        for (Player player : new ArrayList<>(players.values())) {
            player.checkNetwork();
        }

        if ((tickCounter & 0b1111) == 0) {
            titleTick();
            maxTick = 20;
            maxUse = 0;

            if ((tickCounter & 0b111111111) == 0) {
                try {
                    getPluginManager().callEvent(queryRegenerateEvent = new QueryRegenerateEvent(this, 5));
                    if (queryHandler != null) {
                        queryHandler.regenerateInfo();
                    }
                } catch (Exception e) {
                    logger.logException(e);
                }
            }

            getNetwork().updateName();
        }

        if (ServerProperties.auto_save && ++autoSaveTicker >= autoSaveTicks) {
            autoSaveTicker = 0;
            doAutoSave();
        }

        if (sendUsageTicker > 0 && --sendUsageTicker == 0) {
            sendUsageTicker = 6000;
            //todo sendUsage
        }

        if (tickCounter % 100 == 0) {
            for (Level level : levels.values()) {
                level.clearCache();
            }

            if (getTicksPerSecondAverage() < 12) {
                logger.warning(getLanguage().translateString("nukkit.server.tickOverload"));
            }
        }

        //long now = System.currentTimeMillis();
        long nowNano = System.nanoTime();
        //float tick = Math.min(20, 1000 / Math.max(1, now - tickTime));
        //float use = Math.min(1, (now - tickTime) / 50);

        float tick = (float) Math.min(20, 1000000000 / Math.max(1000000, ((double) nowNano - tickTimeNano)));
        float use = (float) Math.min(1, ((double) (nowNano - tickTimeNano)) / 50000000);

        if (maxTick > tick) {
            maxTick = tick;
        }

        if (maxUse < use) {
            maxUse = use;
        }

        System.arraycopy(tickAverage, 1, tickAverage, 0, tickAverage.length - 1);
        tickAverage[tickAverage.length - 1] = tick;

        System.arraycopy(useAverage, 1, useAverage, 0, useAverage.length - 1);
        useAverage[useAverage.length - 1] = use;

        if ((nextTick - tickTime) < -1000) {
            nextTick = tickTime;
        } else {
            nextTick += 50;
        }

        return true;
    }

    public void titleTick() {
        if (!Nukkit.ANSI) {
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        double used = NukkitMath.round((double) (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024, 2);
        double max = NukkitMath.round(((double) runtime.maxMemory()) / 1024 / 1024, 2);
        String usage = Math.round(used / max * 100) + "%";
        String title = (char) 0x1b + "]0;" + getName() + " " +
                getNukkitVersion() +
                " | Online " + players.size() + "/" + ServerProperties.max_players +
                " | Memory " + usage;
        if (!Nukkit.shortTitle) {
            title += " | U " + NukkitMath.round((network.getUpload() / 1024 * 1000), 2)
                    + " D " + NukkitMath.round((network.getDownload() / 1024 * 1000), 2) + " kB/s";
        }
        title += " | TPS " + getTicksPerSecond() +
                " | Load " + getTickUsage() + "%" + (char) 0x07;

        System.out.print(title);

        network.resetStatistics();
    }

    public QueryRegenerateEvent getQueryInformation() {
        return queryRegenerateEvent;
    }

    public String getName() {
        return "Nukkit";
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getNukkitVersion() {
        return Nukkit.VERSION;
    }

    public String getCodename() {
        return Nukkit.CODENAME;
    }

    public String getVersion() {
        return Nukkit.MINECRAFT_VERSION;
    }

    public String getApiVersion() {
        return Nukkit.API_VERSION;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public UUID getServerUniqueId() {
        return serverID;
    }

    public static String getGamemodeString(int mode) {
        switch (mode) {
            case Player.SURVIVAL:
                return "%gameMode.survival";
            case Player.CREATIVE:
                return "%gameMode.creative";
            case Player.ADVENTURE:
                return "%gameMode.adventure";
            case Player.SPECTATOR:
                return "%gameMode.spectator";
        }
        return "UNKNOWN";
    }

    public static int getGamemodeFromString(String str) {
        switch (str.trim().toLowerCase()) {
            case "0":
            case "survival":
            case "s":
                return Player.SURVIVAL;

            case "1":
            case "creative":
            case "c":
                return Player.CREATIVE;

            case "2":
            case "adventure":
            case "a":
                return Player.ADVENTURE;

            case "3":
            case "spectator":
            case "view":
            case "v":
                return Player.SPECTATOR;
        }
        return -1;
    }

    public static int getDifficultyFromString(String str) {
        switch (str.trim().toLowerCase()) {
            case "0":
            case "peaceful":
            case "p":
                return 0;

            case "1":
            case "easy":
            case "e":
                return 1;

            case "2":
            case "normal":
            case "n":
                return 2;

            case "3":
            case "hard":
            case "h":
                return 3;
        }
        return -1;
    }

    public EntityMetadataStore getEntityMetadata() {
        return entityMetadata;
    }

    public PlayerMetadataStore getPlayerMetadata() {
        return playerMetadata;
    }

    public LevelMetadataStore getLevelMetadata() {
        return levelMetadata;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public CraftingManager getCraftingManager() {
        return craftingManager;
    }

    public ServerScheduler getScheduler() {
        return scheduler;
    }

    public int getTick() {
        return tickCounter;
    }

    public float getTicksPerSecond() {
        return ((float) Math.round(maxTick * 100)) / 100;
    }

    public float getTicksPerSecondAverage() {
        float sum = 0;
        int count = tickAverage.length;
        for (float aTickAverage : tickAverage) {
            sum += aTickAverage;
        }
        return (float) NukkitMath.round(sum / count, 2);
    }

    public float getTickUsage() {
        return (float) NukkitMath.round(maxUse * 100, 2);
    }

    public float getTickUsageAverage() {
        float sum = 0;
        int count = useAverage.length;
        for (float aUseAverage : useAverage) {
            sum += aUseAverage;
        }
        return ((float) Math.round(sum / count * 100)) / 100;
    }

    public SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    public Map<UUID, Player> getOnlinePlayers() {
        return new HashMap<>(playerList);
    }

    public void addRecipe(Recipe recipe) {
        craftingManager.registerRecipe(recipe);
    }

    public IPlayer getOfflinePlayer(String name) {
        IPlayer result = getPlayerExact(name.toLowerCase());
        if (result == null) {
            return new OfflinePlayer(this, name);
        }

        return result;
    }

    public CompoundTag getOfflinePlayerData(String name) {
        name = name.toLowerCase();
        String path = getDataPath() + "players/";
        File file = new File(path + name + ".dat");

        if (file.exists()) {
            try {
                return NBTIO.readCompressed(new FileInputStream(file));
            } catch (Exception e) {
                file.renameTo(new File(path + name + ".dat.bak"));
                logger.notice(getLanguage().translateString("nukkit.data.playerCorrupted", name));
            }
        } else {
            logger.notice(getLanguage().translateString("nukkit.data.playerNotFound", name));
        }

        Position spawn = getDefaultLevel().getSafeSpawn();
        CompoundTag nbt = new CompoundTag()
                .putLong("firstPlayed", System.currentTimeMillis() / 1000)
                .putLong("lastPlayed", System.currentTimeMillis() / 1000)
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("0", spawn.x))
                        .add(new DoubleTag("1", spawn.y))
                        .add(new DoubleTag("2", spawn.z)))
                .putString("Level", getDefaultLevel().getName())
                .putList(new ListTag<>("Inventory"))
                .putCompound("Achievements", new CompoundTag())
                .putInt("playerGameType", ServerProperties.gamemode)
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("0", 0))
                        .add(new DoubleTag("1", 0))
                        .add(new DoubleTag("2", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("0", 0))
                        .add(new FloatTag("1", 0)))
                .putFloat("FallDistance", 0)
                .putShort("Fire", 0)
                .putShort("Air", 300)
                .putBoolean("OnGround", true)
                .putBoolean("Invulnerable", false)
                .putString("NameTag", name);

        saveOfflinePlayerData(name, nbt);
        return nbt;
    }

    public void saveOfflinePlayerData(String name, CompoundTag tag) {
        saveOfflinePlayerData(name, tag, false);
    }

    public void saveOfflinePlayerData(String name, CompoundTag tag, boolean async) {
        try {
            if (async) {
                getScheduler().scheduleAsyncTask(new FileWriteTask(getDataPath() + "players/" + name.toLowerCase() + ".dat", NBTIO.writeGZIPCompressed(tag, ByteOrder.BIG_ENDIAN)));
            } else {
                Utils.writeFile(getDataPath() + "players/" + name.toLowerCase() + ".dat", new ByteArrayInputStream(NBTIO.writeGZIPCompressed(tag, ByteOrder.BIG_ENDIAN)));
            }
        } catch (Exception e) {
            logger.critical(getLanguage().translateString("nukkit.data.saveError", new String[]{name, e.getMessage()}));
            if (Nukkit.DEBUG > 1) {
                logger.logException(e);
            }
        }
    }

    public Player getPlayer(String name) {
        Player found = null;
        name = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (Player player : getOnlinePlayers().values()) {
            if (player.getName().toLowerCase().startsWith(name)) {
                int curDelta = player.getName().length() - name.length();
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) {
                    break;
                }
            }
        }

        return found;
    }

    public Player getPlayerExact(String name) {
        name = name.toLowerCase();
        for (Player player : getOnlinePlayers().values()) {
            if (player.getName().toLowerCase().equals(name)) {
                return player;
            }
        }

        return null;
    }

    public Player[] matchPlayer(String partialName) {
        partialName = partialName.toLowerCase();
        List<Player> matchedPlayer = new ArrayList<>();
        for (Player player : getOnlinePlayers().values()) {
            if (player.getName().toLowerCase().equals(partialName)) {
                return new Player[]{player};
            } else if (player.getName().toLowerCase().contains(partialName)) {
                matchedPlayer.add(player);
            }
        }

        return matchedPlayer.toArray(new Player[matchedPlayer.size()]);
    }

    public void removePlayer(Player player) {
        if (identifier.containsKey(player.rawHashCode())) {
            String identifier = this.identifier.get(player.rawHashCode());
            players.remove(identifier);
            this.identifier.remove(player.rawHashCode());
            return;
        }

        for (String identifier : new ArrayList<>(players.keySet())) {
            Player p = players.get(identifier);
            if (player == p) {
                players.remove(identifier);
                this.identifier.remove(player.rawHashCode());
                break;
            }
        }
    }

    public Map<Integer, Level> getLevels() {
        return levels;
    }

    public Level getDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(Level defaultLevel) {
        if (this.defaultLevel == null || (isLevelLoaded(defaultLevel.getFolderName()) && this.defaultLevel != defaultLevel)) {
            this.defaultLevel = defaultLevel;
        }
    }

    public boolean isLevelLoaded(String name) {
        return getLevelByName(name) != null;
    }

    public Level getLevel(int levelId) {
        if (levels.containsKey(levelId)) {
            return levels.get(levelId);
        }
        return null;
    }

    public Level getLevelByName(String name) {
        for (Level level : getLevels().values()) {
            if (level.getFolderName().equals(name)) {
                return level;
            }
        }

        return null;
    }

    public boolean unloadLevel(Level level) {
        return unloadLevel(level, false);
    }

    public boolean unloadLevel(Level level, boolean forceUnload) {
        if (level == getDefaultLevel() && !forceUnload) {
            throw new IllegalStateException("The default level cannot be unloaded while running, please switch levels.");
        }

        return level.unload(forceUnload);

    }

    public boolean loadLevel(String name) {
        if (Objects.equals(name.trim(), "")) {
            throw new LevelException("Invalid empty level name");
        }
        if (isLevelLoaded(name)) {
            return true;
        } else if (!isLevelGenerated(name)) {
            logger.notice(getLanguage().translateString("nukkit.level.notFound", name));

            return false;
        }

        String path = getDataPath() + "worlds/" + name + "/";

        Class<? extends LevelProvider> provider = LevelProviderManager.getProvider(path);

        if (provider == null) {
            logger.error(getLanguage().translateString("nukkit.level.loadError", new String[]{name, "Unknown provider"}));

            return false;
        }

        Level level;
        try {
            level = new Level(this, name, path, provider);
        } catch (Exception e) {
            logger.error(getLanguage().translateString("nukkit.level.loadError", new String[]{name, e.getMessage()}));
            logger.logException(e);
            return false;
        }

        levels.put(level.getId(), level);

        level.initLevel();

        getPluginManager().callEvent(new LevelLoadEvent(level));

        level.setTickRate(ServerProperties.base_tick_rate);

        return true;
    }

    public boolean generateLevel(String name) {
        return generateLevel(name, new java.util.Random().nextLong());
    }

    public boolean generateLevel(String name, long seed) {
        return generateLevel(name, seed, null);
    }

    public boolean generateLevel(String name, long seed, Class<? extends Generator> generator) {
        return generateLevel(name, seed, generator, new HashMap<>());
    }

    public boolean generateLevel(String name, long seed, Class<? extends Generator> generator, Map<String, Object> options) {
        if (Objects.equals(name.trim(), "") || isLevelGenerated(name)) {
            return false;
        }

        if (!options.containsKey("preset")) {
            options.put("preset", ServerProperties.generator_settings);
        }

        if (generator == null) {
            generator = Generator.getGenerator(ServerProperties.level_type);
        }

        Class<? extends LevelProvider> provider;
        if ((provider = LevelProviderManager.getProviderByName(ServerProperties.level_default_format)) == null)
            provider = LevelProviderManager.getProviderByName("mcregion");

        Level level;
        try {
            String path = getDataPath() + "worlds/" + name + "/";

            provider.getMethod("generate", String.class, String.class, long.class, Class.class, Map.class).invoke(null, path, name, seed, generator, options);

            level = new Level(this, name, path, provider);
            levels.put(level.getId(), level);

            level.initLevel();

            level.setTickRate(ServerProperties.base_tick_rate);
        } catch (Exception e) {
            logger.error(getLanguage().translateString("nukkit.level.generationError", new String[]{name, e.getMessage()}));
            logger.logException(e);
            return false;
        }

        getPluginManager().callEvent(new LevelInitEvent(level));

        getPluginManager().callEvent(new LevelLoadEvent(level));

        /*logger.notice(getLanguage().translateString("nukkit.level.backgroundGeneration", name));

        int centerX = (int) level.getSpawnLocation().getX() >> 4;
        int centerZ = (int) level.getSpawnLocation().getZ() >> 4;

        TreeMap<String, Integer> order = new TreeMap<>();

        for (int X = -3; X <= 3; ++X) {
            for (int Z = -3; Z <= 3; ++Z) {
                int distance = X * X + Z * Z;
                int chunkX = X + centerX;
                int chunkZ = Z + centerZ;
                order.put(Level.chunkHash(chunkX, chunkZ), distance);
            }
        }

        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(order.entrySet());

        Collections.sort(sortList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });

        for (String index : order.keySet()) {
            Chunk.Entry entry = Level.getChunkXZ(index);
            level.populateChunk(entry.chunkX, entry.chunkZ, true);
        }*/

        return true;
    }

    public boolean isLevelGenerated(String name) {
        if (Objects.equals(name.trim(), "")) {
            return false;
        }

        String path = getDataPath() + "worlds/" + name + "/";
        if (getLevelByName(name) == null) {

            if (LevelProviderManager.getProvider(path) == null) {
                return false;
            }
        }

        return true;
    }

    public BaseLang getLanguage() {
        return baseLang;
    }

    public boolean isLanguageForced() {
        return forceLanguage;
    }

    public Network getNetwork() {
        return network;
    }

    public PluginIdentifiableCommand getPluginCommand(String name) {
        Command command = commandMap.getCommand(name);
        if (command instanceof PluginIdentifiableCommand) {
            return (PluginIdentifiableCommand) command;
        } else {
            return null;
        }
    }

    public Map<String, List<String>> getCommandAliases() {
        /*
        Object section = getConfig("aliases");
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (section instanceof Map) {
            for (Map.Entry entry : (Set<Map.Entry>) ((Map) section).entrySet()) {
                List<String> commands = new ArrayList<>();
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                if (value instanceof List) {
                    for (String string : (List<String>) value) {
                        commands.add(string);
                    }
                } else {
                    commands.add((String) value);
                }

                result.put(key, commands);
            }
        }

        return result;*/
        return new LinkedHashMap<>();
    }

    public void registerEntities() {
        Entity.registerEntity("Arrow", EntityArrow.class);
        Entity.registerEntity("Item", EntityItem.class);
        Entity.registerEntity("FallingSand", EntityFallingBlock.class);
        Entity.registerEntity("PrimedTnt", EntityPrimedTNT.class);
        Entity.registerEntity("Snowball", EntitySnowball.class);
        Entity.registerEntity("Painting", EntityPainting.class);
        //todo mobs
        Entity.registerEntity("Creeper", EntityCreeper.class);
        //TODO: more mobs
        Entity.registerEntity("Chicken", EntityChicken.class);
        Entity.registerEntity("Cow", EntityCow.class);
        Entity.registerEntity("Pig", EntityPig.class);
        Entity.registerEntity("Rabbit", EntityRabbit.class);
        Entity.registerEntity("Sheep", EntitySheep.class);
        Entity.registerEntity("Wolf", EntityWolf.class);
        Entity.registerEntity("Ocelot", EntityOcelot.class);

        Entity.registerEntity("ThrownExpBottle", EntityExpBottle.class);
        Entity.registerEntity("XpOrb", EntityXPOrb.class);
        Entity.registerEntity("ThrownPotion", EntityPotion.class);

        Entity.registerEntity("Human", EntityHuman.class, true);

        Entity.registerEntity("MinecartRideable", EntityMinecartEmpty.class);
        // TODO: 2016/1/30 all finds of minecart
        Entity.registerEntity("Boat", EntityBoat.class);

        Entity.registerEntity("Lightning", EntityLightning.class);
    }

    public void registerBlockEntities() {
        BlockEntity.registerBlockEntity(BlockEntity.FURNACE, BlockEntityFurnace.class);
        BlockEntity.registerBlockEntity(BlockEntity.CHEST, BlockEntityChest.class);
        BlockEntity.registerBlockEntity(BlockEntity.SIGN, BlockEntitySign.class);
        BlockEntity.registerBlockEntity(BlockEntity.ENCHANT_TABLE, BlockEntityEnchantTable.class);
        BlockEntity.registerBlockEntity(BlockEntity.SKULL, BlockEntitySkull.class);
        BlockEntity.registerBlockEntity(BlockEntity.FLOWER_POT, BlockEntityFlowerPot.class);
        BlockEntity.registerBlockEntity(BlockEntity.BREWING_STAND, BlockEntityBrewingStand.class);
    }

    public static Server getInstance() {
        return instance;
    }

}
