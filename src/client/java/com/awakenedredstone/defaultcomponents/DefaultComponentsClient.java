package com.awakenedredstone.defaultcomponents;

import com.awakenedredstone.defaultcomponents.data.ComponentSync;
import com.awakenedredstone.defaultcomponents.network.DefaultComponentsPresentPayload;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class DefaultComponentsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) -> {
            if (!context.client().isInSingleplayer()) {
                ComponentSync.sync(payload);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ComponentSync.unload();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ComponentSync.unload();
            sender.sendPacket(DefaultComponentsPresentPayload.INSTANCE);
        });

        //TODO: better development/debug tools, the command was too volatile
    }
}
