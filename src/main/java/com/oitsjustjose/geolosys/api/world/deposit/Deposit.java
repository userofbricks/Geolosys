package com.oitsjustjose.geolosys.api.world.deposit;

import com.oitsjustjose.geolosys.api.world.DepositUtils;
import com.oitsjustjose.geolosys.api.world.IDeposit;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.List;
/*
    moved some Deposit class code to this location by Userofbricks
    purpos: to clean up the deposite classes and make them easier to read.
 */
public abstract class Deposit implements IDeposit {
    /*
       Each of the folowing variables has a getter and setter that has
       protected access so only the other deposite classes can use them.
       unless otherwise needed
     */
    private String[] dimFilter;
    private boolean isDimFilterBl;

    /* Optional biome stuff!
       Each has a getter and setter that has
       protected access so only the other deposite classes can use them.
       unless otherwise needed
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
}
