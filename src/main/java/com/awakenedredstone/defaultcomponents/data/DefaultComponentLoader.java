package com.awakenedredstone.defaultcomponents.data;

import com.awakenedredstone.defaultcomponents.duck.ModifyDefaultComponents;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultComponentLoader extends JsonDataLoader<DefaultComponentLoader.ComponentManipulation> implements IdentifiableResourceReloadListener {
    public static final Logger LOGGER = LoggerFactory.getLogger("Default Components Data Parser");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final DefaultComponentLoader INSTANCE = new DefaultComponentLoader();
    ComponentManipulation globalComponents = ComponentManipulation.EMPTY;
    Map<Identifier, ComponentManipulation> itemComponents = Map.of();

    protected DefaultComponentLoader() {
        super(ComponentManipulation.CODEC, ResourceFinder.json("default_components"));
    }

    public ComponentManipulation getGlobalComponents() {
        return globalComponents;
    }

    public Map<Identifier, ComponentManipulation> getItemComponents() {
        return itemComponents;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("default_components", "default_components");
    }

    @Override
    protected void apply(Map<Identifier, ComponentManipulation> prepared, ResourceManager manager, Profiler profiler) {
        final Map<Identifier, ComponentManipulation> perItem = HashMap.newHashMap(0);

        prepared.forEach((identifier, componentMap) -> {
            if (identifier.equals(Identifier.ofVanilla("all"))) {
               globalComponents = componentMap;
               return;
           }

            if (Registries.ITEM.containsId(identifier)) {
                perItem.put(identifier, componentMap);
            } else {
                LOGGER.warn("Skipping invalid item {}", identifier);
            }

            this.itemComponents = Map.copyOf(perItem);
        });

        modifyItems();
    }

    void modifyItems() {
        if (!globalComponents.equals(ComponentManipulation.EMPTY)) {
            for (Item item : Registries.ITEM) {
                if (item instanceof ModifyDefaultComponents modifiable) {
                    Identifier id = Registries.ITEM.getId(item);
                    modifiable.defaultComponents$modifyComponents(getItemComponents().get(id));
                }
            }
        } else {
            itemComponents.forEach((identifier, componentManipulation) -> {
                Item item = Registries.ITEM.get(identifier);
                if (item instanceof ModifyDefaultComponents modifiable) {
                    modifiable.defaultComponents$modifyComponents(getItemComponents().get(identifier));
                }
            });
        }
    }

    public static SyncPayload createSyncPayload() {
        return new SyncPayload(INSTANCE.globalComponents, INSTANCE.getItemComponents());
    }

    public record ComponentManipulation(Optional<Map<ComponentType<?>, Object>> additions, Optional<List<ComponentType<?>>> removals) {
        public static final ComponentManipulation EMPTY = new ComponentManipulation(Optional.empty(), Optional.empty());
        public static final Codec<ComponentManipulation> CODEC = RecordCodecBuilder.create(instance ->
          instance.group(
            ComponentType.TYPE_TO_VALUE_MAP_CODEC.optionalFieldOf("add").forGetter(ComponentManipulation::additions),
            ComponentType.CODEC.listOf().optionalFieldOf("remove").forGetter(ComponentManipulation::removals)
          ).apply(instance, ComponentManipulation::new)
        );
        public static final PacketCodec<RegistryByteBuf, ComponentManipulation> PACKET_CODEC = new PacketCodec<>() {

            @Override
            public void encode(RegistryByteBuf buf, ComponentManipulation payload) {
                Map<ComponentType<?>, Object> additions = payload.additions().orElse(Map.of());
                buf.writeInt(additions.size());
                additions.forEach((type, value) -> {
                    ComponentType.PACKET_CODEC.encode(buf, type);
                    encodeComponent(buf, type, value);
                });

                List<ComponentType<?>> removals = payload.removals().orElse(List.of());
                buf.writeInt(removals.size());
                for (ComponentType<?> removal : removals) {
                    ComponentType.PACKET_CODEC.encode(buf, removal);
                }
            }

            @Override
            public ComponentManipulation decode(RegistryByteBuf buf) {
                int additionCount = buf.readInt();
                Map<ComponentType<?>, Object> additions = HashMap.newHashMap(additionCount);
                for (int i = 0; i < additionCount; i++) {
                    ComponentType<?> type = ComponentType.PACKET_CODEC.decode(buf);
                    Object value = type.getPacketCodec().decode(buf);
                    additions.put(type, value);
                }

                int removalCount = buf.readInt();
                List<ComponentType<?>> removals = new ArrayList<>(removalCount);
                for (int i = 0; i < removalCount; i++) {
                    removals.add(ComponentType.PACKET_CODEC.decode(buf));
                }

                Optional<Map<ComponentType<?>, Object>> add;
                if (additions.isEmpty()) {
                    add = Optional.empty();
                } else {
                    add = Optional.of(Map.copyOf(additions));
                }

                Optional<List<ComponentType<?>>> remove;
                if (removals.isEmpty()) {
                    remove = Optional.empty();
                } else {
                    remove = Optional.of(List.copyOf(removals));
                }

                return new DefaultComponentLoader.ComponentManipulation(add, remove);
            }

            private static <T> void encodeComponent(RegistryByteBuf buf, ComponentType<T> type, Object value) {
                type.getPacketCodec().encode(buf, (T) value);
            }
        };

        public boolean isRemoved(ComponentType<?> type) {
            return removals.isPresent() && removals.get().contains(type);
        }

        @SuppressWarnings("unchecked")
        public <C extends ComponentType<T>, T> void forEachAdded(BiConsumer<C, T> action) {
            if (additions().isPresent()) {
                additions().get().forEach((componentType, o) -> action.accept((C) componentType, (T) o));
            }
        }

        @SuppressWarnings("unchecked")
        public <C extends ComponentType<?>> void forEachRemoved(Consumer<C> action) {
            if (removals().isPresent()) {
                removals().get().forEach((componentType) -> action.accept((C) componentType));
            }
        }
    }
}
