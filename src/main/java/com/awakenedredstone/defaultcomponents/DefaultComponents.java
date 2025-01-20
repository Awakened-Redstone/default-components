package com.awakenedredstone.defaultcomponents;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import com.awakenedredstone.defaultcomponents.duck.RebuildDefaultComponents;
import com.awakenedredstone.defaultcomponents.mixin.MergedComponentMapAccessor;
import com.awakenedredstone.defaultcomponents.network.DefaultComponentsPresentPayload;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;
import com.awakenedredstone.defaultcomponents.util.ConcurrentWeakSet;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DefaultComponents implements ModInitializer {
    public static final String MOD_ID = "default_components";
    public static final Logger LOGGER = LoggerFactory.getLogger("Default Components");
    public static final Set<GameProfile> MODDED_PLAYERS = new ConcurrentWeakSet<>(0);
    public static final Set<RebuildDefaultComponents> ITEM_STACKS = new ConcurrentWeakSet<>(1024);

    @Override
    public void onInitialize() {
        //TODO: Make packets to trick vanilla clients

        ResourceManagerHelper resourceManagerHelper = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        resourceManagerHelper.registerReloadListener(DefaultComponentLoader.INSTANCE);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            try {
                synchronized (ITEM_STACKS) {
                    ITEM_STACKS.forEach(RebuildDefaultComponents::defaultComponents$rebuildComponents);
                }

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(player, DefaultComponentLoader.createSyncPayload());
                }
            } catch (Throwable e) {
                LOGGER.error("WHAT IN THE WORLD", e);
            }
        });

        PayloadTypeRegistry.playS2C().register(SyncPayload.ID, SyncPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DefaultComponentsPresentPayload.ID, DefaultComponentsPresentPayload.PACKET_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(DefaultComponentLoader.createSyncPayload()));

        ServerPlayNetworking.registerGlobalReceiver(DefaultComponentsPresentPayload.ID, (payload, context) -> {
            LOGGER.info("Modded Player {} joined", context.player().getName().getString());
            MODDED_PLAYERS.add(context.player().getGameProfile());
        });

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                dispatcher.register(
                  CommandManager.literal("default_components")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        if (!source.isExecutedByPlayer() || source.getPlayer().getMainHandStack().isEmpty()) return 0;

                        ComponentMap baseComponents = ((MergedComponentMapAccessor) source.getPlayer().getMainHandStack().getComponents()).getBaseComponents();
                        NbtElement nbt = ComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, baseComponents).getOrThrow();

                        source.sendFeedback(() -> NbtHelper.toPrettyPrintedText(nbt),false);

                        return baseComponents.size();
                    })
                );
            });
        }
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
