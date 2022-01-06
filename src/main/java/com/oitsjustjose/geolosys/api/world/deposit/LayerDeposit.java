package com.oitsjustjose.geolosys.api.world.deposit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.oitsjustjose.geolosys.Geolosys;
import com.oitsjustjose.geolosys.api.world.DepositUtils;
import com.oitsjustjose.geolosys.api.world.IDeposit;
import com.oitsjustjose.geolosys.common.config.CommonConfig;
import com.oitsjustjose.geolosys.common.data.serializer.SerializerUtils;
import com.oitsjustjose.geolosys.common.utils.Utils;
import com.oitsjustjose.geolosys.common.world.SampleUtils;
import com.oitsjustjose.geolosys.common.world.capability.IDepositCapability;
import com.oitsjustjose.geolosys.common.world.feature.FeatureUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.BiomeDictionary;

public class LayerDeposit extends Deposit implements IDeposit {
    public static final String JSON_TYPE = "geolosys:deposit_layer";

    private int yMin;
    private int yMax;
    private int radius;
    private int depth;

    public LayerDeposit(HashMap<String, HashMap<BlockState, Float>> oreBlocks, HashMap<BlockState, Float> sampleBlocks,
            int yMin,
            int yMax, int radius, int depth, int genWt, String[] dimFilter, boolean isDimFilterBl,
            @Nullable List<BiomeDictionary.Type> biomeTypes, @Nullable List<Biome> biomeFilter,
            @Nullable boolean isBiomeFilterBl, HashSet<BlockState> blockStateMatchers) {
        super(oreBlocks, sampleBlocks, genWt, dimFilter, isDimFilterBl, biomeTypes, biomeFilter, isBiomeFilterBl, blockStateMatchers);
        this.yMin = yMin;
        this.yMax = yMax;
        this.radius = radius;
        this.depth = depth;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Layer deposit with Blocks=");
        ret.append(this.getAllOres());
        ret.append(", Samples=");
        ret.append(Arrays.toString(this.getSampleToWtMap().keySet().toArray()));
        ret.append(", Y Range=[");
        ret.append(this.yMin);
        ret.append(",");
        ret.append(this.yMax);
        ret.append("], Radius=");
        ret.append(this.radius);
        ret.append(", Depth=");
        ret.append(this.depth);
        return ret.toString();
    }

    /**
     * Handles full-on generation of this type of pluton. Requires 0 arguments as
     * everything is self-contained in this class
     * 
     * @return (int) the number of pluton resource blocks placed. If 0 -- this
     *         should be evaluted as a false for use of Mojang's sort-of sketchy
     *         generation code in
     *         ]
     */
    @Override
    public int generate(WorldGenLevel level, BlockPos pos, IDepositCapability cap) {
        /* Dimension checking is done in PlutonRegistry#pick */
        /* Check biome allowance */
        if (!DepositUtils.canPlaceInBiome(level.getBiome(pos), this.getBiomeFilter(), this.getBiomeTypeFilter(),
                this.isBiomeFilterBl())) {
            return 0;
        }

        int totlPlaced = 0;

        ChunkPos thisChunk = new ChunkPos(pos);

        int x = ((thisChunk.getMinBlockX() + thisChunk.getMaxBlockX()) / 2) - level.getRandom().nextInt(8)
                + level.getRandom().nextInt(16);
        int y = this.yMin + level.getRandom().nextInt(Math.abs(this.yMax - this.yMin));
        int z = ((thisChunk.getMinBlockZ() + thisChunk.getMaxBlockZ()) / 2) - level.getRandom().nextInt(8)
                + level.getRandom().nextInt(16);
        int max = Utils.getTopSolidBlock(level, pos).getY();
        if (y > max) {
            y = Math.max(yMin, max);
        }

        BlockPos basePos = new BlockPos(x, y, z);

        for (int dX = -this.radius; dX <= this.radius; dX++) {
            for (int dZ = -this.radius; dZ <= this.radius; dZ++) {
                for (int dY = 0; dY < depth; dY++) {
                    float dist = dX * dX + dZ * dZ;
                    if (dist > this.radius * 2) {
                        continue;
                    }

                    BlockPos placePos = basePos.offset(dX, dY, dZ);
                    BlockState current = level.getBlockState(placePos);
                    BlockState tmp = this.getOre(current);
                    if (tmp == null) {
                        continue;
                    }

                    // Skip this block if it can't replace the target block or doesn't have a
                    // manually-configured replacer in the blocks object
                    if (!(this.getBlockStateMatchers().contains(current)
                            || this.getOreToWtMap().containsKey(current.getBlock().getRegistryName().toString()))) {
                        continue;
                    }

                    if (FeatureUtils.tryPlaceBlock(level, thisChunk, placePos, tmp, cap)) {
                        totlPlaced++;
                    }
                }
            }
        }
        return totlPlaced;
    }

    /**
     * Handles what to do after the world has generated
     */
    @Override
    public void afterGen(WorldGenLevel level, BlockPos pos, IDepositCapability cap) {
        // Debug the pluton
        if (CommonConfig.DEBUG_WORLD_GEN.get()) {
            Geolosys.getInstance().LOGGER.info("Generated {} in Chunk {} (Pos [{} {} {}])", this.toString(),
                    new ChunkPos(pos), pos.getX(), pos.getY(), pos.getZ());
        }

        ChunkPos thisChunk = new ChunkPos(pos);
        int maxSampleCnt = Math.min(CommonConfig.MAX_SAMPLES_PER_CHUNK.get(),
                (this.radius / CommonConfig.MAX_SAMPLES_PER_CHUNK.get())
                        + (this.radius % CommonConfig.MAX_SAMPLES_PER_CHUNK.get()));
        for (int i = 0; i < maxSampleCnt; i++) {
            BlockPos samplePos = SampleUtils.getSamplePosition(level, new ChunkPos(pos));
            BlockState tmp = this.getSample();

            if (tmp == null) {
                continue;
            }

            if (samplePos == null || SampleUtils.inNonWaterFluid(level, samplePos)) {
                continue;
            }

            if (SampleUtils.isInWater(level, samplePos) && tmp.hasProperty(BlockStateProperties.WATERLOGGED)) {
                tmp = tmp.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
            }

            FeatureUtils.tryPlaceBlock(level, thisChunk, samplePos, tmp, cap);
            FeatureUtils.fixSnowyBlock(level, samplePos);
        }
    }

    public static LayerDeposit deserialize(JsonObject json, JsonDeserializationContext ctx) {
        if (json == null) {
            return null;
        }

        try {
            // Plutons 101 -- basics and intro to getting one gen'd
            HashMap<String, HashMap<BlockState, Float>> oreBlocks = SerializerUtils
                    .buildMultiBlockMatcherMap(json.get("blocks").getAsJsonObject());
            HashMap<BlockState, Float> sampleBlocks = SerializerUtils
                    .buildMultiBlockMap(json.get("samples").getAsJsonArray());
            int yMin = json.get("yMin").getAsInt();
            int yMax = json.get("yMax").getAsInt();
            int radius = json.get("radius").getAsInt();
            int depth = json.get("depth").getAsInt();
            int genWt = json.get("generationWeight").getAsInt();

            // Dimensions
            String[] dimFilter = SerializerUtils.getDimFilter(json);
            boolean isDimFilterBl = SerializerUtils.getIsDimFilterBl(json);

            // Biomes
            boolean isBiomeFilterBl = true;
            List<BiomeDictionary.Type> biomeTypeFilter = null;
            List<Biome> biomeFilter = null;
            if (json.has("biomes")) {
                String[] biomeArrRaw = SerializerUtils.getBiomeFilter(json);
                isBiomeFilterBl = SerializerUtils.getIsBiomeFilterBl(json);
                biomeTypeFilter = SerializerUtils.extractBiomeTypes(biomeArrRaw);
                biomeFilter = SerializerUtils.extractBiomes(biomeArrRaw);
            }

            // Block State Matchers
            HashSet<BlockState> blockStateMatchers = DepositUtils.getDefaultMatchers();
            if (json.has("blockStateMatchers")) {
                blockStateMatchers = SerializerUtils.toBlockStateList(json.get("blockStateMatchers").getAsJsonArray());
            }

            return new LayerDeposit(oreBlocks, sampleBlocks, yMin, yMax, radius, depth, genWt, dimFilter, isDimFilterBl,
                    biomeTypeFilter, biomeFilter, isBiomeFilterBl, blockStateMatchers);
        } catch (Exception e) {
            Geolosys.getInstance().LOGGER.error("Failed to parse: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public JsonElement serialize(LayerDeposit dep, JsonSerializationContext ctx) {
        JsonObject json = new JsonObject();
        JsonObject config = new JsonObject();
        JsonParser parser = new JsonParser();

        // Custom logic for the biome filtering
        JsonObject biomes = new JsonObject();
        biomes.addProperty("isBlacklist", this.isBiomeFilterBl());
        biomes.add("filter", SerializerUtils.deconstructBiomes(this.getBiomeFilter(), this.getBiomeTypeFilter()));

        // Custom logic for the dimension filtering
        JsonObject dimensions = new JsonObject();
        dimensions.addProperty("isBlacklist", this.isDimensionFilterBl());
        dimensions.add("filter", parser.parse(Arrays.toString(this.getDimensionFilter())));

        // Add basics of Plutons
        config.add("blocks", SerializerUtils.deconstructMultiBlockMatcherMap(this.getOreToWtMap()));
        config.add("samples", SerializerUtils.deconstructMultiBlockMap(this.getSampleToWtMap()));
        config.addProperty("yMin", this.yMin);
        config.addProperty("yMax", this.yMax);
        config.addProperty("radius", this.radius);
        config.addProperty("depth", this.depth);
        config.addProperty("generationWeight", this.getGenWt());
        config.add("dimensions", dimensions);
        config.add("biomes", biomes);

        // Glue the two parts of this together.
        json.addProperty("type", JSON_TYPE);
        json.add("config", config);
        return json;
    }
}
