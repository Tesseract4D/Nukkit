package cn.nukkit.level.generator;

import cn.nukkit.block.*;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.level.generator.biome.BiomeSelector;
import cn.nukkit.level.generator.noise.Simplex;
import cn.nukkit.level.generator.object.ore.OreType;
import cn.nukkit.level.generator.structures.Structure;
import cn.nukkit.level.generator.structures.StructureGlowstone;
import cn.nukkit.level.generator.structures.StructureGroundFire;
import cn.nukkit.level.generator.structures.StructureOre;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hell extends Generator {
    @Override
    public int getId() {
        return TYPE_NETHER;
    }

    private List<Structure> structures = new ArrayList<>();

    private ChunkManager level;

    private NukkitRandom random;

    private List<Structure> generationStructures = new ArrayList<>();

    private final int waterHeight = 16;
    private final double emptyHeight = 56;
    private final double emptyAmplitude = 1;
    private final double density = 0.5;
    private final int bedrockDepth = 5;
    private Simplex noiseBase;

    private BiomeSelector selector;
    public Hell(Map<String, Object> options) {
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.random = random;
        this.random.setSeed(this.level.getSeed());
        this.noiseBase = new Simplex(this.random, 4, (double) 1 / 4, (double) 1 / 64);
        StructureOre ores = new StructureOre();
        ores.setOreTypes(new OreType[]{
                new OreType(new BlockOreQuartz(), 20, 16, 0, 128),
                new OreType(new BlockSoulSand(), 5, 64, 0, 128),
                new OreType(new BlockGravel(), 5, 64, 0, 128),
                new OreType(new BlockLava(), 1, 16, 0, this.waterHeight),
        });
        this.structures.add(ores);
        this.structures.add(new StructureGlowstone());
        StructureGroundFire groundFire = new StructureGroundFire();
        groundFire.setBaseAmount(1);
        groundFire.setRandomAmount(1);
        this.structures.add(groundFire);
        //lava = new NetherLava();
        //lava.setBaseAmount(0);
        //lava.setRandomAmount(0);
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        this.random.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());

        double[][][] noise = Generator.getFastNoise3D(this.noiseBase, 16, 128, 16, 4, 8, 4, chunkX * 16, 0, chunkZ * 16);

        FullChunk chunk = this.level.getChunk(chunkX, chunkZ);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {

                Biome biome = Biome.getBiome(Biome.OCEAN);
                chunk.setBiomeId(x, z, biome.getId());
                int bColor = biome.getColor();

                chunk.setBiomeColor(x, z, bColor >> 16, (bColor >> 8) & 0xff, bColor & 0xff);

                for (int y = 0; y < 128; ++y) {
                    if (y == 0 || y == 127) {
                        chunk.setBlockId(x, y, z, Block.BEDROCK);
                        continue;
                    }
                    double noiseValue = (Math.abs(this.emptyHeight - y) / this.emptyHeight) * this.emptyAmplitude - noise[x][z][y];
                    noiseValue -= 1 - this.density;

                    if (noiseValue > 0) {
                        chunk.setBlockId(x, y, z, Block.NETHERRACK);
                    } else if (y <= this.waterHeight) {
                        chunk.setBlockId(x, y, z, Block.STILL_LAVA);
                        chunk.setBlockLight(x, y + 1, z, 15);
                    }
                }
            }
        }

        for (Structure structure : this.generationStructures) {
            structure.generate(this.level, chunkX, chunkZ, this.random);
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        this.random.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        for (Structure structure : this.structures) {
            structure.generate(this.level, chunkX, chunkZ, this.random);
        }

        FullChunk chunk = this.level.getChunk(chunkX, chunkZ);
        Biome biome = Biome.getBiome(chunk.getBiomeId(7, 7));
        biome.populateChunk(this.level, chunkX, chunkZ, this.random);
    }

    @Override
    public Map<String, Object> getSettings() {
        return new HashMap<>();
    }

    @Override
    public String getName() {
        return "Nether";
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(127.5, 128, 127.5);
    }

    @Override
    public ChunkManager getChunkManager() {
        return level;
    }
}
