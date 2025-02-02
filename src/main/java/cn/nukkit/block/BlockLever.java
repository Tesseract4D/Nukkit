package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.sound.LeverSound;
import cn.nukkit.redstone.Redstone;

/**
 * @author Nukkit Project Team
 */
public class BlockLever extends BlockFlowable {

    public BlockLever(int meta) {
        super(meta);
        this.setPowerSource(true);
        this.setPowerLevel(Redstone.POWER_STRONGEST);
    }

    public BlockLever() {
        this(0);
    }

    @Override
    public String getName() {
        return "Lever";
    }

    @Override
    public int getId() {
        return LEVER;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public double getHardness() {
        return 0.5D;
    }

    @Override
    public double getResistance() {
        return 2.5D;
    }

    @Override
    public int[][] getDrops(Item item) {
        return new int[][]{
                {Item.LEVER, 0, 1}
        };
    }

    public boolean isPowerOn() {
        return this.meta >= 8;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        this.meta ^= 0x08;

        this.getLevel().setBlock(this, this, true, false);
        this.getLevel().addSound(new LeverSound(this, this.isPowerOn()));
        if (this.isPowerOn()) {
            this.setPowerSource(true);
            Redstone.active(this);
        } else {
            Redstone.deactive(this, this.getPowerLevel());
        }
        return true;
    }

    @Override
    public boolean place(Item item, Block block, Block target, int face, double fx, double fy, double fz, Player player) {
        if (!target.isTransparent()) {
            int to;

            if (face == 0) {
                to = player != null ? player.getDirection() : 0;
                this.meta = (to % 2 != 1 ? 0 : 7);
            } else if (face == 1) {
                to = player != null ? player.getDirection() : 0;
                this.meta = (to % 2 != 1 ? 6 : 5);
            } else {
                this.meta = 6 - face;
            }

            this.getLevel().setBlock(block, this, true, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onBreak(Item item) {
        super.onBreak(item);
        int level = this.getPowerLevel();
        Redstone.deactive(this, level);
        return true;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            int side = this.getDamage();
            if (this.isActivated()) side ^= 0x08;
            int[] faces = new int[]{1, 4, 5, 2, 3, 0, 0, 1};

            Block block = this.getSide(faces[side]);
            if (block.isTransparent()) {
                this.getLevel().useBreakOn(this);

                return Level.BLOCK_UPDATE_NORMAL;
            }
        }
        return 0;
    }

    public boolean isActivated() {
        return (this.meta & 0x08) == 0x08;
    }
}
