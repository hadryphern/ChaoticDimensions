package net.blue.chaoticd.network;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Login-stage compatibility gate for the extended ItemStack wire format.
 *
 * <p>Counts above 127 use a Chaotic Dimensions packet extension. A peer that
 * cannot decode that extension must never reach play state, otherwise the
 * first large stack could desynchronize the connection. The login query is
 * intentionally independent from item registration so it protects dedicated
 * servers as well as the integrated server.</p>
 */
public final class StackSizeProtocol {
    public static final ResourceLocation CHANNEL = new ResourceLocation(
        ChaoticDimensions.MOD_ID,
        "stack_size_protocol"
    );

    /** Increment when the extended ItemStack wire representation changes. */
    public static final int VERSION = 1;

    private StackSizeProtocol() {
    }

    public static void initializeServer() {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            FriendlyByteBuf request = PacketByteBufs.create();
            request.writeVarInt(VERSION);
            sender.sendPacket(CHANNEL, request);
        });

        ServerLoginNetworking.registerGlobalReceiver(
            CHANNEL,
            (server, handler, understood, buffer, synchronizer, responseSender) -> {
                if (!understood) {
                    handler.disconnect(Component.translatable(
                        "disconnect.chaoticd.stack_protocol_required"
                    ));
                    return;
                }

                int clientVersion;
                try {
                    clientVersion = buffer.readVarInt();
                } catch (RuntimeException exception) {
                    handler.disconnect(Component.translatable(
                        "disconnect.chaoticd.stack_protocol_required"
                    ));
                    return;
                }

                if (clientVersion != VERSION) {
                    handler.disconnect(Component.translatable(
                        "disconnect.chaoticd.stack_protocol_version",
                        VERSION,
                        clientVersion
                    ));
                }
            }
        );
    }
}
