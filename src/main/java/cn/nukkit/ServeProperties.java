package cn.nukkit;

import java.util.Base64;
import java.util.UUID;

public class ServeProperties {
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
    public static boolean nether_enabled = true;
    public static String nether_name = "nether";
    public static boolean enable_query = true;
    public static boolean enable_rcon = false;
    public static String rcon_password = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().replace("-", "").getBytes()).substring(3, 13);
    public static boolean auto_save = true;
}
