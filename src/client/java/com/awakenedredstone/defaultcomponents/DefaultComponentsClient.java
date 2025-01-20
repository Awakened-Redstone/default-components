package com.awakenedredstone.defaultcomponents;

import com.awakenedredstone.defaultcomponents.data.ComponentSync;
import com.awakenedredstone.defaultcomponents.mixin.MergedComponentMapAccessor;
import com.awakenedredstone.defaultcomponents.network.DefaultComponentsPresentPayload;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;

public class DefaultComponentsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) -> {
            ComponentSync.sync(payload);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ComponentSync.unload();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sender.sendPacket(DefaultComponentsPresentPayload.INSTANCE);
        });

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(
                  ClientCommandManager.literal("client_default_components")
                    .executes(context -> {
                        FabricClientCommandSource source = context.getSource();
                        if (source.getPlayer().getMainHandStack().isEmpty()) return 0;

                        ComponentMap baseComponents = ((MergedComponentMapAccessor) source.getPlayer().getMainHandStack().getComponents()).getBaseComponents();
                        NbtElement nbt = ComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, baseComponents).getOrThrow();

                        source.sendFeedback(NbtHelper.toPrettyPrintedText(nbt));

                        return baseComponents.size();
                    })
                );
            });
        }
    }
}
