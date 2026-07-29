package net.blue.chaoticd.client;

import java.util.concurrent.CompletableFuture;
import net.blue.chaoticd.network.StackSizeProtocol;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

/** Client half of the login gate required by the 999-stack packet format. */
public final class StackSizeProtocolClient {
    private StackSizeProtocolClient() {
    }

    public static void initialize() {
        ClientLoginNetworking.registerGlobalReceiver(
            StackSizeProtocol.CHANNEL,
            (client, handler, request, listenerAdder) -> {
                // Consume the server declaration when present. The server is
                // authoritative for compatibility and compares our response.
                if (request.isReadable()) {
                    request.readVarInt();
                }

                FriendlyByteBuf response = PacketByteBufs.create();
                response.writeVarInt(StackSizeProtocol.VERSION);
                return CompletableFuture.completedFuture(response);
            }
        );
    }
}
