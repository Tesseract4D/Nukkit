package cn.nukkit.level.generator.biome;

import cn.nukkit.level.generator.structures.StructureGrass;
import cn.nukkit.level.generator.structures.StructureSugarcane;
import cn.nukkit.level.generator.structures.StructureTallGrass;
import cn.nukkit.level.generator.structures.StructureTallSugarcane;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class RiverBiome extends WateryBiome {

    public RiverBiome() {
        super();

        StructureSugarcane sugarcane = new StructureSugarcane();
        sugarcane.setBaseAmount(6);
        StructureTallSugarcane tallSugarcane = new StructureTallSugarcane();
        tallSugarcane.setBaseAmount(60);

        StructureGrass grass = new StructureGrass();
        grass.setBaseAmount(30);
        this.addPopulator(grass);

        StructureTallGrass tallGrass = new StructureTallGrass();
        tallGrass.setBaseAmount(5);

        this.addPopulator(tallGrass);
        this.addPopulator(sugarcane);
        this.addPopulator(tallSugarcane);

        this.setElevation(58, 62);

        this.temperature = 0.5;
        this.rainfall = 0.7;
    }

    @Override
    public String getName() {
        return "River";
    }
}
