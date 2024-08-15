package cn.nukkit.level.generator.biome;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFlower;
import cn.nukkit.level.generator.structures.*;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class PlainBiome extends GrassyBiome {

    public PlainBiome() {
        super();
        StructureSugarcane sugarcane = new StructureSugarcane();
        sugarcane.setBaseAmount(6);
        StructureTallSugarcane tallSugarcane = new StructureTallSugarcane();
        tallSugarcane.setBaseAmount(60);
        StructureGrass grass = new StructureGrass();
        grass.setBaseAmount(40);
        StructureTallGrass tallGrass = new StructureTallGrass();
        tallGrass.setBaseAmount(7);
        StructureFlower flower = new StructureFlower();
        flower.setBaseAmount(10);
        flower.addType(Block.DANDELION, 0);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_POPPY);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_AZURE_BLUET);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_RED_TULIP);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_ORANGE_TULIP);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_WHITE_TULIP);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_PINK_TULIP);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_OXEYE_DAISY);

        this.addPopulator(sugarcane);
        this.addPopulator(tallSugarcane);
        this.addPopulator(grass);
        this.addPopulator(tallGrass);
        this.addPopulator(flower);


        this.setElevation(63, 74);

        this.temperature = 0.8;
        this.rainfall = 0.4;
    }

    @Override
    public String getName() {
        return "Plains";
    }
}
