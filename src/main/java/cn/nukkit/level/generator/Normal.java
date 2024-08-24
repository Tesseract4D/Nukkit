package cn.nukkit.level.generator;

import cn.nukkit.block.*;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.level.generator.biome.BiomeSelector;
import cn.nukkit.level.generator.noise.Simplex;
import cn.nukkit.level.generator.object.ore.OreType;
import cn.nukkit.level.generator.structures.Structure;
import cn.nukkit.level.generator.structures.StructureOre;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class Normal extends Generator {


    @Override
    public int getId() {
        return TYPE_INFINITE;
    }

    private final List<Structure> structures = new ArrayList<>();
    private ChunkManager level;
    private NukkitRandom random;
    private Simplex noiseBase;

    private BiomeSelector selector;
    public final int waterHeight = 62;
    private static final double[][] GAUSSIAN_KERNEL;
    private static final int SMOOTH_SIZE = 2;

    static {
        int s = SMOOTH_SIZE * 2 + 1;
        GAUSSIAN_KERNEL = new double[s][s];
        generateKernel();
    }
    public Normal() {
        this(new HashMap<>());
    }

    public Normal(Map<String, Object> options) {
        //Nothing here. Just used for future update.
    }

    private static void generateKernel() {

        double bellSize = 1d / SMOOTH_SIZE;
        double bellHeight = 2 * SMOOTH_SIZE;
        int s = SMOOTH_SIZE;

        for (int sx = -s; sx <= s; ++sx) {
            for (int sz = -s; sz <= s; ++sz) {
                double bx = bellSize * sx;
                double bz = bellSize * sz;
                GAUSSIAN_KERNEL[sx + s][sz + s] = bellHeight * Math.exp(-(bx * bx + bz * bz) / 2);
            }
        }
    }
    @Override
    public ChunkManager getChunkManager() {
        return level;
    }

    @Override
    public String getName() {
        return "normal";
    }

    @Override
    public Map<String, Object> getSettings() {
        return new HashMap<>();
    }

    public Biome pickBiome(int x, int z) {
        long hash = x * 2345803L ^ z * 9236449L ^ this.level.getSeed();
        hash *= hash + 223;

        long xNoise = hash >> 20 & 3;
        long zNoise = hash >> 22 & 3;

        if (xNoise == 3) {
            xNoise = 1;
        }
        if (zNoise == 3) {
            zNoise = 1;
        }

        return this.selector.pickBiome(x + xNoise - 1, z + zNoise - 1);
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.random = random;
        this.random.setSeed(this.level.getSeed());
        this.noiseBase = new Simplex(this.random, 4F, 1F / 4F, 1F / 32F);
        this.random.setSeed(this.level.getSeed());
        this.selector = new BiomeSelector(this.random, Biome.getBiome(Biome.OCEAN));

        this.selector.addBiome(Biome.getBiome(Biome.OCEAN));
        this.selector.addBiome(Biome.getBiome(Biome.PLAINS));
        this.selector.addBiome(Biome.getBiome(Biome.DESERT));
        this.selector.addBiome(Biome.getBiome(Biome.MOUNTAINS));
        this.selector.addBiome(Biome.getBiome(Biome.FOREST));
        this.selector.addBiome(Biome.getBiome(Biome.TAIGA));
        this.selector.addBiome(Biome.getBiome(Biome.SWAMP));
        this.selector.addBiome(Biome.getBiome(Biome.RIVER));
        this.selector.addBiome(Biome.getBiome(Biome.ICE_PLAINS));
        this.selector.addBiome(Biome.getBiome(Biome.SMALL_MOUNTAINS));
        this.selector.addBiome(Biome.getBiome(Biome.BIRCH_FOREST));

        this.selector.recalculate();

        StructureOre ores = new StructureOre();
        ores.setOreTypes(new OreType[]{
                new OreType(new BlockOreCoal(), 20, 16, 0, 128),
                new OreType(new BlockOreIron(), 20, 8, 0, 64),
                new OreType(new BlockOreRedstone(), 8, 7, 0, 16),
                new OreType(new BlockOreLapis(), 1, 6, 0, 32),
                new OreType(new BlockOreGold(), 2, 8, 0, 32),
                new OreType(new BlockOreDiamond(), 1, 7, 0, 16),
                new OreType(new BlockDirt(), 20, 32, 0, 128),
                new OreType(new BlockGravel(), 10, 16, 0, 128)
        });
        this.structures.add(ores);
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        this.random.setSeed(0xdeadbeef ^ ((long) chunkX << 8) ^ chunkZ ^ this.level.getSeed());

        double[][][] noise = Generator.getFastNoise3D(this.noiseBase, 16, 128, 16, 4, 8, 4, chunkX * 16, 0, chunkZ * 16);

        FullChunk chunk = this.level.getChunk(chunkX, chunkZ);

        HashMap<String, Biome> biomeCache = new HashMap<>();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double minSum = 0;
                double maxSum = 0;
                double weightSum = 0;

                Biome biome = this.pickBiome(chunkX * 16 + x, chunkZ * 16 + z);
                chunk.setBiomeId(x, z, biome.getId());
                double[] color = {0, 0, 0};

                for (int sx = -SMOOTH_SIZE; sx <= SMOOTH_SIZE; sx++) {
                    for (int sz = -SMOOTH_SIZE; sz <= SMOOTH_SIZE; sz++) {

                        double weight = GAUSSIAN_KERNEL[sx + SMOOTH_SIZE][sz + SMOOTH_SIZE];
                        Biome adjacent;
                        if (sx == 0 && sz == 0) {
                            adjacent = biome;
                        } else {
                            String index = Level.chunkHash(chunkX * 16 + x + sx, chunkZ * 16 + z + sz);
                            if (biomeCache.containsKey(index)) {
                                adjacent = biomeCache.get(index);
                            } else {
                                adjacent = this.pickBiome(chunkX * 16 + x + sx, chunkZ * 16 + z + sz);
                                biomeCache.put(index, adjacent);
                            }
                        }

                        minSum += ((adjacent.getMinElevation() - 1) * weight);
                        maxSum += (adjacent.getMaxElevation() * weight);
                        int bColor = adjacent.getColor();
                        int c = bColor >> 16;
                        color[0] += c * c * weight;
                        c = (bColor >> 8) & 0xff;
                        color[1] += c * c * weight;
                        c = bColor & 0xff;
                        color[2] += c * c * weight;

                        weightSum += weight;
                    }
                }

                minSum /= weightSum;
                maxSum /= weightSum;

                chunk.setBiomeColor(x, z, (int) Math.sqrt(color[0] / weightSum), (int) Math.sqrt(color[1] / weightSum), (int) Math.sqrt(color[2] / weightSum));

                boolean solidLand = false;
                for (int y = 127; y >= 0; --y) {
                    if (y == 0) {
                        chunk.setBlockId(x, y, z, Block.BEDROCK);
                        continue;
                    }

                    // A noiseAdjustment of 1 will guarantee ground, a noiseAdjustment of -1 will guarantee air.
                    //effHeight = min(y - smoothHeight - minSum,
                    double noiseAdjustment = 2 * ((maxSum - y) / (maxSum - minSum)) - 1;


                    // To generate caves, we bring the noiseAdjustment down away from 1.
                    double caveLevel = minSum - 10;
                    double distAboveCaveLevel = Math.max(0, y - caveLevel); // must be positive

                    noiseAdjustment = Math.min(noiseAdjustment, 0.4 + (distAboveCaveLevel / 10));
                    double noiseValue = noise[x][z][y] + noiseAdjustment;

                    if (noiseValue > 0) {
                        chunk.setBlockId(x, y, z, Block.STONE);
                        solidLand = true;
                    } else if (y <= this.waterHeight && !solidLand) {
                        chunk.setBlockId(x, y, z, Block.STILL_WATER);
                    }
                }
            }
        }
        buildSurface(chunk);
    }

    public void buildSurface(FullChunk chunk) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                Biome biome = Biome.getBiome(chunk.getBiomeId(x, z));
                Block[] cover = biome.getGroundCover();
                if (cover != null && cover.length > 0) {
                    int diffY = 0;
                    if (!cover[0].isSolid()) {
                        diffY = 1;
                    }

                    byte[] column = chunk.getBlockIdColumn(x, z);
                    int y;
                    for (y = 127; y > 0; --y) {
                        if (column[y] != 0x00 && !Block.get(column[y] & 0xff).isTransparent()) {
                            break;
                        }
                    }
                    int startY = Math.min(127, y + diffY);
                    int endY = startY - cover.length;
                    for (y = startY; y > endY && y >= 0; --y) {
                        Block b = cover[startY - y];
                        if (column[y] == 0x00 && b.isSolid()) {
                            break;
                        }
                        if (b.getDamage() == 0) {
                            chunk.setBlockId(x, y, z, b.getId());
                        } else {
                            chunk.setBlock(x, y, z, b.getId(), b.getDamage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        this.random.setSeed(0xdeadbeef ^ ((long) chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        for (Structure structure : this.structures) {
            structure.generate(this.level, chunkX, chunkZ, this.random);
        }

        FullChunk chunk = this.level.getChunk(chunkX, chunkZ);
        Biome biome = Biome.getBiome(chunk.getBiomeId(7, 7));
        biome.populateChunk(this.level, chunkX, chunkZ, this.random);
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(127.5, 128, 127.5);
    }
}
