package cn.nukkit;

import cn.nukkit.lang.BaseLang;

import java.util.Base64;
import java.util.UUID;

public class ServerProperties {
    public static String motd = "Nukkit Server For Minecraft: PE";
    public static int server_port = 19132;
    public static String server_ip = "0.0.0.0";
    public static int view_distance = 10;
    public static boolean white_list = false;
    public static boolean announce_achievements = true;
    public static int spawn_protection = 16;
    public static int max_players = 20;
    public static boolean allow_flight = false;
    public static boolean spawn_animals = true;
    public static boolean spawn_mobs = true;
    public static int gamemode = 0;
    public static boolean force_gamemode = false;
    public static boolean hardcore = false;
    public static boolean pvp = true;
    public static int difficulty = 1;
    public static String generator_settings = "";
    public static boolean enable_structures = true;
    public static String level_name = "world";
    public static String level_seed = "";
    public static String level_type = "DEFAULT";
    public static String level_default_format = "mcregion";
    public static boolean nether_enabled = true;
    public static String nether_name = "nether";
    public static boolean enable_query = true;
    public static boolean enable_rcon = false;
    public static String rcon_password = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().replace("-", "").getBytes()).substring(3, 13);
    public static boolean auto_save = true;
    public static String language = BaseLang.FALLBACK_LANGUAGE;
    public static String shutdown_message = "服务器已关闭";
    public static boolean force_language = false;
    public static boolean query_plugins = true;
    public static boolean deprecated_verbose = true;
    public static int async_workers = -1;
    public static boolean network_compression_async = true;
    public static int network_compression_level = 7;
    public static int network_batch_threshold = 256;
    public static int debug_level = 1;
    public static boolean debug_commands = false;
    public static int chunks_sending_per_tick = 4;
    public static boolean chunks_sending_cache_chunks = false;
    public static int chunks_ticking_per_tick = 40;
    public static int chunks_ticking_radius = 4;
    public static boolean chunks_ticking_light_updates = false;
    public static boolean clear_chunks_on_tick = false;
    public static int chunk_generation_queue_size = 8;
    public static int chunk_population_queue_size = 2;
    public static int spawn_threshold = 256;
    public static boolean auto_tick_rate = true;
    public static int auto_tick_rate_limit = 20;
    public static boolean always_tick_players = false;
    public static int base_tick_rate = 1;

}
