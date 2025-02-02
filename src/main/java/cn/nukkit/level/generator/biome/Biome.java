package cn.nukkit.level.generator.biome;

import cn.nukkit.block.Block;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.generator.structures.Structure;
import cn.nukkit.math.NukkitRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class Biome {


    public static final int OCEAN = 0;
    public static final int PLAINS = 1;
    public static final int DESERT = 2;
    public static final int MOUNTAINS = 3;
    public static final int FOREST = 4;
    public static final int TAIGA = 5;
    public static final int SWAMP = 6;
    public static final int RIVER = 7;

    public static final int ICE_PLAINS = 12;


    public static final int SMALL_MOUNTAINS = 20;


    public static final int BIRCH_FOREST = 27;


    public static final int MAX_BIOMES = 256;

    private static Map<Integer, Biome> biomes = new HashMap<>();

    private int id;
    private boolean registered = false;

    private ArrayList<Structure> structures = new ArrayList<>();

    private int minElevation;
    private int maxElevation;

    private Block[] groundCover;

    protected double rainfall = 0.5;
    protected double temperature = 0.5;
    protected int grassColor = 0;

    protected static void register(int id, Biome biome) {
        biome.setId(id);
        biome.grassColor = generateBiomeColor(biome.getTemperature(), biome.getRainfall());
        biomes.put(id, biome);
    }

    public static void init() {
        register(OCEAN, new OceanBiome());
        register(PLAINS, new PlainBiome());
        register(DESERT, new DesertBiome());
        register(MOUNTAINS, new MountainsBiome());
        register(FOREST, new ForestBiome());
        register(TAIGA, new TaigaBiome());
        register(SWAMP, new SwampBiome());
        register(RIVER, new RiverBiome());
        register(ICE_PLAINS, new IcePlainsBiome());
        register(SMALL_MOUNTAINS, new SmallMountainsBiome());
        register(BIRCH_FOREST, new ForestBiome(ForestBiome.TYPE_BIRCH));
    }

    public static Biome getBiome(int id) {
        return biomes.containsKey(id) ? biomes.get(id) : biomes.get(OCEAN);
    }

    public void clearStructures() {
        this.structures.clear();
    }

    public void addPopulator(Structure structure) {
        this.structures.add(structure);
    }

    public void populateChunk(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random) {
        for (Structure structure : structures) {
            structure.generate(level, chunkX, chunkZ, random);
        }
    }

    public ArrayList<Structure> getStructures() {
        return structures;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract String getName();

    public int getMinElevation() {
        return minElevation;
    }

    public int getMaxElevation() {
        return maxElevation;
    }

    public void setElevation(int min, int max) {
        this.minElevation = min;
        this.maxElevation = max;
    }

    public Block[] getGroundCover() {
        return groundCover;
    }

    public void setGroundCover(Block[] covers) {
        this.groundCover = covers;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getRainfall() {
        return rainfall;
    }

    private static int generateBiomeColor(double temperature, double rainfall) {
        double x = (1 - temperature) * 255;
        double z = (1 - rainfall * temperature) * 255;
        double[] c = interpolateColor(256, x, z, new double[]{0x47, 0xd0, 0x33}, new double[]{0x6c, 0xb4, 0x93}, new double[]{0xbf, 0xb6, 0x55}, new double[]{0x80, 0xb4, 0x97});
        return ((int) c[0] << 16) | ((int) c[1] << 8) | (int) (c[2]);
    }


    private static double[] interpolateColor(double size, double x, double z, double[] c1, double[] c2, double[] c3, double[] c4) {
        double[] l1 = lerpColor(c1, c2, x / size);
        double[] l2 = lerpColor(c3, c4, x / size);

        return lerpColor(l1, l2, z / size);
    }

    private static double[] lerpColor(double[] a, double[] b, double s) {
        double invs = 1 - s;
        return new double[]{a[0] * invs + b[0] * s, a[1] * invs + b[1] * s, a[2] * invs + b[2] * s};
    }

    abstract public int getColor();
}
