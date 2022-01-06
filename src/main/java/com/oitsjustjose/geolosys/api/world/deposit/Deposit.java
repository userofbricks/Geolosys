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
import java.util.Map;

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

    /* Hashmap of blockMatcher.getRegistryName(): sumWt */
    private HashMap<String, Float> cumulOreWtMap = new HashMap<>();
    private float sumWtSamples = 0.0F;

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

        // Verify that blocks.default exists.
        if (!this.oreToWtMap.containsKey("default")) {
            throw new RuntimeException("Pluton blocks should always have a default key");
        }

        for (Map.Entry<String, HashMap<BlockState, Float>> i : this.oreToWtMap.entrySet()) {
            if (!this.cumulOreWtMap.containsKey(i.getKey())) {
                this.cumulOreWtMap.put(i.getKey(), 0.0F);
            }

            for (Map.Entry<BlockState, Float> j : i.getValue().entrySet()) {
                float v = this.cumulOreWtMap.get(i.getKey());
                this.cumulOreWtMap.put(i.getKey(), v + j.getValue());
            }

            if (this.cumulOreWtMap.get(i.getKey()) != 1.0F) {
                throw new RuntimeException("Sum of weights for pluton blocks should equal 1.0");
            }
        }

        for (Map.Entry<BlockState, Float> e : this.sampleToWtMap.entrySet()) {
            this.sumWtSamples += e.getValue();
        }

        if (sumWtSamples != 1.0F) {
            throw new RuntimeException("Sum of weights for pluton samples should equal 1.0");
        }
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

    protected HashMap<String, HashMap<BlockState, Float>> getOreToWtMap() {
        return oreToWtMap;
    }
    protected void setOreToWtMap(HashMap<String, HashMap<BlockState, Float>> oreToWtMap) {
        this.oreToWtMap = oreToWtMap;
    }
    protected HashMap<BlockState, Float> getSampleToWtMap() {
        return sampleToWtMap;
    }
    protected void setSampleToWtMap(HashMap<BlockState, Float> sampleToWtMap) {
        this.sampleToWtMap = sampleToWtMap;
    }

    /**
     * Uses {@link DepositUtils#pick(HashMap, float)} to find a random ore block to
     * return.
     *
     * @return the random ore block chosen (based on weight) Can be null to
     *         represent "density" of the ore -- null results should be used to
     *         determine if the block in the world should be replaced. If null,
     *         don't replace 😉
     */
    @Nullable
    public BlockState getOre(BlockState currentState) {
        String res = currentState.getBlock().getRegistryName().toString();
        if (this.oreToWtMap.containsKey(res)) {
            // Return a choice from a specialized set here
            HashMap<BlockState, Float> mp = this.oreToWtMap.get(res);
            return DepositUtils.pick(mp, this.cumulOreWtMap.get(res));
        }
        return DepositUtils.pick(this.oreToWtMap.get("default"), this.cumulOreWtMap.get("default"));
    }

    /**
     * Uses {@link DepositUtils#pick(HashMap, float)} to find a random pluton sample
     * to return.
     *
     * @return the random pluton sample chosen (based on weight) Can be null to
     *         represent "density" of the samples -- null results should be used to
     *         determine if the sample in the world should be replaced. If null,
     *         don't replace 😉
     */
    @Nullable
    public BlockState getSample() {
        return DepositUtils.pick(this.getSampleToWtMap(), this.sumWtSamples);
    }
}
