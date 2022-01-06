package com.oitsjustjose.geolosys.api.world.deposit;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.List;
/*
    moved to location by Userofbricks
    purpos: to clean up the deposite classes and make them easier to read.
 */
public class Deposit {

    /* Optional biome stuff!
       Each has a getter and setter that is protected so only the other deposite classes can use them.
     */
    @Nullable
    private List<BiomeDictionary.Type> biomeTypeFilter;

    @Nullable
    private List<Biome> biomeFilter;

    @Nullable
    private boolean isBiomeFilterBl;

    @Nullable
    protected List<BiomeDictionary.Type> getBiomeTypeFilter() {
        return biomeTypeFilter;
    }

    protected void setBiomeTypeFilter(@Nullable List<BiomeDictionary.Type> biomeTypeFilter) {
        this.biomeTypeFilter = biomeTypeFilter;
    }

    @Nullable
    protected List<Biome> getBiomeFilter() {
        return biomeFilter;
    }

    protected void setBiomeFilter(@Nullable List<Biome> biomeFilter) {
        this.biomeFilter = biomeFilter;
    }

    @Nullable
    protected boolean isBiomeFilterBl() {
        return isBiomeFilterBl;
    }

    protected void setBiomeFilterBl(@Nullable boolean biomeFilterBl) {
        isBiomeFilterBl = biomeFilterBl;
    }
}
