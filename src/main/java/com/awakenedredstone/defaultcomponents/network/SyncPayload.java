package com.awakenedredstone.defaultcomponents.network;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncPayload(DefaultComponentLoader.ComponentManipulation globalComponents, Map<Identifier, DefaultComponentLoader.ComponentManipulation> itemComponents) implements CustomPayload {
    public static final Id<SyncPayload> ID = new CustomPayload.Id<>(DefaultComponents.id("sync"));
    public static final PacketCodec<RegistryByteBuf, SyncPayload> PACKET_CODEC = new PacketCodec<>() {

        @Override
        public void encode(RegistryByteBuf buf, SyncPayload payload) {
            DefaultComponentLoader.ComponentManipulation.PACKET_CODEC.encode(buf, payload.globalComponents);

            buf.writeInt(payload.itemComponents.size());
            payload.itemComponents.forEach((id, manipulation) -> {
                Identifier.PACKET_CODEC.encode(buf, id);
                DefaultComponentLoader.ComponentManipulation.PACKET_CODEC.encode(buf, manipulation);
            });
        }

        @Override
        public SyncPayload decode(RegistryByteBuf buf) {
            DefaultComponentLoader.ComponentManipulation global = DefaultComponentLoader.ComponentManipulation.PACKET_CODEC.decode(buf);

            int itemCount = buf.readInt();
            Map<Identifier, DefaultComponentLoader.ComponentManipulation> itemComponents = HashMap.newHashMap(itemCount);
            for (int i = 0; i < itemCount; i++) {
                Identifier identifier = Identifier.PACKET_CODEC.decode(buf);
                DefaultComponentLoader.ComponentManipulation components = DefaultComponentLoader.ComponentManipulation.PACKET_CODEC.decode(buf);
                itemComponents.put(identifier, components);
            }

            return new SyncPayload(global, Map.copyOf(itemComponents));
        }
    };

    @Override
    public Id<SyncPayload> getId() {
        return ID;
    }
}
