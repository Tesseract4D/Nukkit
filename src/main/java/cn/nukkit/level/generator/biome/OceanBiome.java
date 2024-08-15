package cn.nukkit.level.generator.biome;

import cn.nukkit.block.Block;
import cn.nukkit.level.generator.structures.StructureSugarcane;
import cn.nukkit.level.generator.structures.StructureTallSugarcane;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class OceanBiome extends WateryBiome {

    public OceanBiome() {
        super();

        StructureSugarcane sugarcane = new StructureSugarcane();
        sugarcane.setBaseAmount(6);
        StructureTallSugarcane tallSugarcane = new StructureTallSugarcane();
        tallSugarcane.setBaseAmount(60);
        this.addPopulator(sugarcane);
        this.addPopulator(tallSugarcane);
        this.setElevation(46, 58);

        this.temperature = 0.5;
        this.rainfall = 0.5;

    }

    @Override
    public Block[] getGroundCover() {
        return super.getGroundCover();
    }

    @Override
    public String getName() {
        return "Ocean";
    }
}
