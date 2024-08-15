package cn.nukkit.level.generator.biome;

import cn.nukkit.block.BlockSapling;
import cn.nukkit.level.generator.structures.StructureGrass;
import cn.nukkit.level.generator.structures.StructureTallGrass;
import cn.nukkit.level.generator.structures.StructureTree;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class TaigaBiome extends SnowyBiome {

    public TaigaBiome() {
        super();

        StructureGrass grass = new StructureGrass();
        grass.setBaseAmount(6);
        this.addPopulator(grass);

        StructureTree trees = new StructureTree(BlockSapling.SPRUCE);
        trees.setBaseAmount(10);
        this.addPopulator(trees);

        StructureTallGrass tallGrass = new StructureTallGrass();
        tallGrass.setBaseAmount(1);

        this.addPopulator(tallGrass);

        this.setElevation(63, 81);

        this.temperature = 0.05;
        this.rainfall = 0.8;
    }

    @Override
    public String getName() {
        return "Taiga";
    }
}
