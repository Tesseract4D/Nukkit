package cn.nukkit.level.generator.structures;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.math.NukkitRandom;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class Structure {
    public abstract void generate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random);
}
