package cn.nukkit.level.generator.biome;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFlower;
import cn.nukkit.block.BlockSapling;
import cn.nukkit.level.generator.structures.StructureFlower;
import cn.nukkit.level.generator.structures.StructureLilyPad;
import cn.nukkit.level.generator.structures.StructureTree;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class SwampBiome extends GrassyBiome {

    public SwampBiome() {
        super();

        StructureLilyPad lilypad = new StructureLilyPad();
        lilypad.setBaseAmount(4);

        StructureTree trees = new StructureTree(BlockSapling.OAK);
        trees.setBaseAmount(2);

        StructureFlower flower = new StructureFlower();
        flower.setBaseAmount(2);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_BLUE_ORCHID);

        this.addPopulator(trees);
        this.addPopulator(flower);
        this.addPopulator(lilypad);

        this.setElevation(62, 63);

        this.temperature = 0.8;
        this.rainfall = 0.9;
    }

    @Override
    public String getName() {
        return "Swamp";
    }

    @Override
    public int getColor() {
        return 0x6a7039;
    }
}
