package com.oitsjustjose.geolosys.api.world.deposit;

import com.oitsjustjose.geolosys.api.world.DepositUtils;
import com.oitsjustjose.geolosys.api.world.IDeposit;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
/*
    moved some Deposit class code to this location by Userofbricks
    purpos: to clean up the deposite classes and make them easier to read.
 */
public abstract class Deposit implements IDeposit {
    /*
       Each of the folowing variables has a getter and setter that has
       protected access so only the other deposite classes can use them.
       unless otherwise needed or unneeded
     */
    private HashMap<String, HashMap<BlockState, Float>> oreToWtMap = new HashMap<>();
    private HashMap<BlockState, Float> sampleToWtMap = new HashMap<>();

    private String[] dimFilter;
    private boolean isDimFilterBl;
    private int genWt;
    private HashSet<BlockState> blockStateMatchers;
    // Optional biome stuff!
    @Nullable
    private List<BiomeDictionary.Type> biomeTypeFilter;
    @Nullable
    private List<Biome> biomeFilter;
    @Nullable
    private boolean isBiomeFilterBl;

    public Deposit (HashMap<String, HashMap<BlockState, Float>> oreBlocks,
                    HashMap<BlockState, Float> sampleBlocks, int genWt, String[] dimFilter, boolean isDimFilterBl,
                    @Nullable List<BiomeDictionary.Type> biomeTypes, @Nullable List<Biome> biomeFilter,
                    @Nullable boolean isBiomeFilterBl, HashSet<BlockState> blockStateMatchers) {
        this.oreToWtMap = oreBlocks;
        this.sampleToWtMap = sampleBlocks;

        this.genWt = genWt;
        this.dimFilter = dimFilter;
        this.isDimFilterBl = isDimFilterBl;
        this.biomeTypeFilter = biomeTypes;
        this.isBiomeFilterBl =isBiomeFilterBl;
        this.blockStateMatchers = blockStateMatchers;
        this.biomeFilter = biomeFilter;
    }

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
    @Override
    public boolean canPlaceInBiome(Biome b) {
        return DepositUtils.canPlaceInBiome(b, this.getBiomeFilter(), this.getBiomeTypeFilter(), this.isBiomeFilterBl());
    }
    @Override
    public boolean hasBiomeRestrictions() {
        return this.getBiomeFilter() != null || this.getBiomeTypeFilter() != null;
    }
    @Override
    public boolean isDimensionFilterBl() {
        return isDimFilterBl;
    }
    protected void setDimFilterBl(boolean dimFilterBl) {
        isDimFilterBl = dimFilterBl;
    }

    @Override
    public String[] getDimensionFilter() {
        return dimFilter;
    }
    protected void setDimFilter(String[] dimFilter) {
        this.dimFilter = dimFilter;
    }

    @Override
    public int getGenWt() {
        return this.genWt;
    }
    protected void setGenWt(int genWt) {
        this.genWt = genWt;
    }

    @Override
    public HashSet<BlockState> getBlockStateMatchers() {
        return this.blockStateMatchers == null ? DepositUtils.getDefaultMatchers() : this.blockStateMatchers;
    }
    protected void setBlockStateMatchers(HashSet<BlockState> blockStateMatchers) {
        this.blockStateMatchers = blockStateMatchers;
    }

    public HashMap<String, HashMap<BlockState, Float>> getOreToWtMap() {
        return oreToWtMap;
    }

    public void setOreToWtMap(HashMap<String, HashMap<BlockState, Float>> oreToWtMap) {
        this.oreToWtMap = oreToWtMap;
    }

    public HashMap<BlockState, Float> getSampleToWtMap() {
        return sampleToWtMap;
    }

    public void setSampleToWtMap(HashMap<BlockState, Float> sampleToWtMap) {
        this.sampleToWtMap = sampleToWtMap;
    }
}
