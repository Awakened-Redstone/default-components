package com.awakenedredstone.defaultcomponents;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentData;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import com.awakenedredstone.defaultcomponents.network.DefaultComponentsPresentPayload;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;
import com.awakenedredstone.defaultcomponents.util.ConcurrentWeakSet;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DefaultComponents implements ModInitializer {
    public static final String MOD_ID = "default_components";
    public static final Logger LOGGER = LoggerFactory.getLogger("Default Components");
    public static final Set<GameProfile> MODDED_PLAYERS = new ConcurrentWeakSet<>(0);

    @Override
    public void onInitialize() {
        //TODO: Make packets to trick vanilla clients

        ResourceManagerHelper resourceManagerHelper = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        resourceManagerHelper.registerReloadListener(id("default_components"), DefaultComponentLoader::new);

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {

            if (success) {
                try {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        ServerPlayNetworking.send(player, DefaultComponentData.createSyncPayload());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to update default components", e);
                }
            }
        });

        PayloadTypeRegistry.playS2C().register(SyncPayload.ID, SyncPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DefaultComponentsPresentPayload.ID, DefaultComponentsPresentPayload.PACKET_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(DefaultComponentData.createSyncPayload()));

        ServerPlayNetworking.registerGlobalReceiver(DefaultComponentsPresentPayload.ID, (payload, context) -> {
            MODDED_PLAYERS.add(context.player().getGameProfile());
        });


    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
