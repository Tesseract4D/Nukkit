package cn.nukkit.level.generator.biome;

import cn.nukkit.level.generator.noise.Simplex;
import cn.nukkit.math.NukkitRandom;

import java.util.HashMap;
import java.util.Map;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class BiomeSelector {
    private Biome fallback;
    private Simplex temperature;
    private Simplex rainfall;

    private Map<Integer, Biome> biomes = new HashMap<>();

    private int[] map = new int[64 * 64];

    public BiomeSelector(NukkitRandom random, Biome fallback) {
        this.fallback = fallback;
        this.temperature = new Simplex(random, 2F, 1F / 16F, 1F / 512F);
        this.rainfall = new Simplex(random, 2F, 1F / 16F, 1F / 512F);
    }

    public int lookup(double temperature, double rainfall) {
        if (rainfall < 0.25) {
            return Biome.SWAMP;
        } else if (rainfall < 0.60) {
            if (temperature < 0.25) {
                return Biome.ICE_PLAINS;
            } else if (temperature < 0.75) {
                return Biome.PLAINS;
            } else {
                return Biome.DESERT;
            }
        } else if (rainfall < 0.80) {
            if (temperature < 0.25) {
                return Biome.TAIGA;
            } else if (temperature < 0.75) {
                return Biome.FOREST;
            } else {
                return Biome.BIRCH_FOREST;
            }
        } else {
            if (temperature < 0.25) {
                return Biome.MOUNTAINS;
            } else {
                return Biome.SMALL_MOUNTAINS;
            }
        }
    }

    public void recalculate() {
        this.map = new int[64 * 64];
        for (int i = 0; i < 64; ++i) {
            for (int j = 0; j < 64; ++j) {
                this.map[i + (j << 6)] = this.lookup(i / 63d, j / 63d);
            }
        }
    }

    public void addBiome(Biome biome) {
        this.biomes.put(biome.getId(), biome);
    }

    public double getTemperature(double x, double z) {
        return (this.temperature.noise2D(x, z, true) + 1) / 2;
    }

    public double getRainfall(double x, double z) {
        return (this.rainfall.noise2D(x, z, true) + 1) / 2;
    }

    public Biome pickBiome(double x, double z) {
        int temperature = (int) (this.getTemperature(x, z) * 63);
        int rainfall = (int) (this.getRainfall(x, z) * 63);

        int biomeId = this.map[temperature + (rainfall << 6)];
        return this.biomes.containsKey(biomeId) ? this.biomes.get(biomeId) : this.fallback;
    }

}
