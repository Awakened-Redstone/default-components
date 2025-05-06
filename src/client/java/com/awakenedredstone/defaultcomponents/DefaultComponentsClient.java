package com.awakenedredstone.defaultcomponents;

import com.awakenedredstone.defaultcomponents.data.ComponentSync;
import com.awakenedredstone.defaultcomponents.network.DefaultComponentsPresentPayload;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultComponentsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Default Components Client");

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) -> {
            ComponentSync.sync(payload);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ComponentSync.unload());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ComponentSync.unload();
            sender.sendPacket(DefaultComponentsPresentPayload.INSTANCE);
        });

        //TODO: better development/debug tools, the command was too volatile
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
              ClientCommandManager.literal("client-default-components")
                .executes(context -> {
                    FabricClientCommandSource source = context.getSource();

                    DynamicRegistryManager registryManager = MinecraftClient.getInstance().player.getRegistryManager();
                    ComponentMap components = source.getPlayer().getMainHandStack().getItem().getComponents();
                    LOGGER.info("{}", components);

                    NbtElement nbtElement = ComponentMap.CODEC.encodeStart(registryManager.getOps(NbtOps.INSTANCE), components).getOrThrow();

                    source.sendFeedback(NbtHelper.toPrettyPrintedText(nbtElement));

                    return 0;
                }).then(
                  ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                    .executes(context -> {
                        FabricClientCommandSource source = context.getSource();

                        ItemStackArgument itemArgument = ItemStackArgumentType.getItemStackArgument(context, "item");

                        DynamicRegistryManager registryManager = MinecraftClient.getInstance().player.getRegistryManager();
                        ComponentMap components = itemArgument.getItem().getComponents();
                        LOGGER.info("{}", components);

                        NbtElement nbtElement = ComponentMap.CODEC.encodeStart(registryManager.getOps(NbtOps.INSTANCE), components).getOrThrow();

                        source.sendFeedback(NbtHelper.toPrettyPrintedText(nbtElement));
                        return 0;
                    })
                )
            );
        });
    }
}
