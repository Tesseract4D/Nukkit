package cn.nukkit.level.generator.structures;

import cn.nukkit.block.Block;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.math.NukkitRandom;

public class StructureGroundFire extends Structure {
    private ChunkManager level;
    private int randomAmount;
    private int baseAmount;

    public void setRandomAmount(int amount) {
        this.randomAmount = amount;
    }

    public void setBaseAmount(int amount) {
        this.baseAmount = amount;
    }

    @Override
    public void generate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random) {
        this.level = level;
        int amount = random.nextRange(0, this.randomAmount + 1) + this.baseAmount;
        for (int i = 0; i < amount; ++i) {
            int x = random.nextRange(chunkX * 16, chunkX * 16 + 15);
            int z = random.nextRange(chunkZ * 16, chunkZ * 16 + 15);
            int y = this.getHighestWorkableBlock(x, z);
            if (y != -1 && this.canGroundFireStay(x, y, z)) {
                this.level.setBlockIdAt(x, y, z, Block.FIRE);
                this.level.updateBlockLight(x, y, z);
            }
        }
    }

    private boolean canGroundFireStay(int x, int y, int z) {
        int b = this.level.getBlockIdAt(x, y, z);
        return (b == Block.AIR || b == Block.SNOW_LAYER) && this.level.getBlockIdAt(x, y - 1, z) == 87;
    }

    private int getHighestWorkableBlock(int x, int z) {
        int y = 0;
        for (; y <= 127; ++y)
            if (this.level.getBlockIdAt(x, y, z) == Block.AIR)
                break;
        return y == 0 ? -1 : y;
    }
}
