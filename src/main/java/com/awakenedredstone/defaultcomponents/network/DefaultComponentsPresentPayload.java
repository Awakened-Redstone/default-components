package com.awakenedredstone.defaultcomponents.network;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record DefaultComponentsPresentPayload() implements CustomPayload {
    public static final DefaultComponentsPresentPayload INSTANCE = new DefaultComponentsPresentPayload();
    public static final Id<DefaultComponentsPresentPayload> ID = new Id<>(DefaultComponents.id("mod_present"));
    public static final PacketCodec<RegistryByteBuf, DefaultComponentsPresentPayload> PACKET_CODEC = new PacketCodec<>() {

        @Override
        public void encode(RegistryByteBuf buf, DefaultComponentsPresentPayload payload) {}

        @Override
        public DefaultComponentsPresentPayload decode(RegistryByteBuf buf) {
            return new DefaultComponentsPresentPayload();
        }
    };

    @Override
    public Id<DefaultComponentsPresentPayload> getId() {
        return ID;
    }
}
