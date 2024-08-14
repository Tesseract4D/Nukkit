package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.block.BlockSpreadEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.object.ObjectTallGrass;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BlockColor;

/**
 * author: Angelic47
 * Nukkit Project
 */
public class BlockGrass extends BlockDirt {

    public BlockGrass() {
        this(0);
    }

    public BlockGrass(int meta) {
        super(0);
    }

    @Override
    public int getId() {
        return GRASS;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public double getHardness() {
        return 0.6;
    }

    @Override
    public double getResistance() {
        return 3;
    }

    @Override
    public String getName() {
        return "Grass";
    }

    @Override
    public boolean onActivate(Item item) {
        return this.onActivate(item, null);
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (item.getId() == Item.DYE && item.getDamage() == 0x0F) {
            item.count--;
            ObjectTallGrass.growGrass(this.getLevel(), this, new NukkitRandom(), 15, 10);
            return true;
        } else if (item.isHoe()) {
            item.useOn(this);
            this.getLevel().setBlock(this, new BlockFarmland());
            return true;
        } else if (item.isShovel()) {
            item.useOn(this);
            this.getLevel().setBlock(this, new BlockGrassPath());
            return true;
        }

        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            Level lv = this.getLevel();
            Block block = lv.getBlock(new Vector3(this.x, this.y, this.z));
            int u = (int) this.x, v = (int) this.y, w = (int) this.z;
            int light = lv.getLightAt(u, v, w);
            if (light < 4 || !(lv.getBlock(new Vector3(this.x, this.y + 1, this.z)) instanceof BlockTransparent)) {
                BlockSpreadEvent ev = new BlockSpreadEvent(block, this, new BlockDirt());
                Server.getInstance().getPluginManager().callEvent(ev);
                if (!ev.isCancelled()) {
                    lv.setBlock(this, ev.getNewState());
                }
            } else if (light >= 9) {
                for (int l = 0; l < 4; ++l) {
                    NukkitRandom random = new NukkitRandom();
                    int x = random.nextRange(u - 1, u + 1);
                    int y = random.nextRange(v - 2, v + 2);
                    int z = random.nextRange(w - 1, w + 1);
                    Block blocks = lv.getBlock(new Vector3(x, y, z));
                    if (blocks.getId() == Block.DIRT && blocks.getDamage() == 0 && lv.getLightAt(x, y, z) >= 4 && !(blocks.getSide(1) instanceof BlockTransparent)) {
                        BlockSpreadEvent ev = new BlockSpreadEvent(blocks, this, new BlockGrass());
                        Server.getInstance().getPluginManager().callEvent(ev);
                        if (!ev.isCancelled()) {
                            lv.setBlock(blocks, ev.getNewState());
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.GRASS_BLOCK_COLOR;
    }
}
