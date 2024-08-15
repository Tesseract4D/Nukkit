package cn.nukkit.level.generator.biome;

import cn.nukkit.level.generator.structures.StructureGrass;
import cn.nukkit.level.generator.structures.StructureTallGrass;
import cn.nukkit.level.generator.structures.StructureTree;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class MountainsBiome extends GrassyBiome {

    public MountainsBiome() {
        super();

        StructureTree tree = new StructureTree();
        tree.setBaseAmount(1);
        this.addPopulator(tree);

        StructureGrass grass = new StructureGrass();
        grass.setBaseAmount(30);
        this.addPopulator(grass);

        StructureTallGrass tallGrass = new StructureTallGrass();
        tallGrass.setBaseAmount(1);
        this.addPopulator(tallGrass);

        this.setElevation(63, 127);

        this.temperature = 0.4;
        this.rainfall = 0.5;
    }

    @Override
    public String getName() {
        return "Mountains";
    }
}
