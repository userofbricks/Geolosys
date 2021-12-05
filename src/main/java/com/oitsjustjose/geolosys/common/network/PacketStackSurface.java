package com.oitsjustjose.geolosys.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.function.Supplier;

public class PacketStackSurface {

    public HashSet<BlockState> blocks;

    public PacketStackSurface(FriendlyByteBuf buf) {
        CompoundTag comp = buf.readNbt();
        this.blocks = PacketHelpers.decodeBlocks(comp);
    }

    public PacketStackSurface(HashSet<BlockState> d1) {
        this.blocks = d1;
    }

    public static PacketStackSurface decode(FriendlyByteBuf buf) {
        return new PacketStackSurface(buf);
    }

    public static void encode(PacketStackSurface msg, FriendlyByteBuf buf) {
        buf.writeNbt(PacketHelpers.encodeBlocks(msg.blocks));
    }

    public void handleServer(Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleClient(PacketStackSurface msg, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            context.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                sendProspectingMessage(mc.player, PacketHelpers.messagify(msg.blocks));
            });
        }
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void sendProspectingMessage(Player player, Object... messageDecorators) {
        TranslatableComponent msg = new TranslatableComponent("geolosys.pro_pick.tooltip.found_surface",
                messageDecorators);
        player.displayClientMessage(msg, true);
    }
}
