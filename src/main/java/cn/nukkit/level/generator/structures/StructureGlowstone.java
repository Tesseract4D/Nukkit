package cn.nukkit.level.generator.structures;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockGlowstone;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.generator.object.ore.ObjectOre;
import cn.nukkit.level.generator.object.ore.OreType;
import cn.nukkit.math.NukkitRandom;

public class StructureGlowstone extends Structure {
    private ChunkManager level;

    @Override
    public void generate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random) {
        this.level = level;
        OreType type = new OreType(new BlockGlowstone(), 1, 20, 128, 10);
        ObjectOre ore = new ObjectOre(random, type);
        ore.base = 0;
        for (int i = 0; i < ore.type.clusterCount; ++i) {
            int x = random.nextRange(chunkX << 4, (chunkX << 4) + 15);
            int z = random.nextRange(chunkZ << 4, (chunkZ << 4) + 15);
            int y = this.getHighestWorkableBlock(x, z);
            if (y != -1)
                ore.placeObject(level, x, y, z);
        }
    }

    private int getHighestWorkableBlock(int x, int z) {
        int y = 127;
        for (; y >= 0; --y)
            if (this.level.getBlockIdAt(x, y, z) == 0)
                break;
        return y == 0 ? -1 : y + 1;
    }
}
