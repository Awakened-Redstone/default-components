package com.awakenedredstone.defaultcomponents.mixin;

import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayNetworkAddon.class)
public interface ServerPlayNetworkAddonAccessor {
    @Accessor ServerPlayNetworkHandler getHandler();
}
