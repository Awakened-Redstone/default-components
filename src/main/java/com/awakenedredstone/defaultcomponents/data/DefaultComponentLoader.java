package com.awakenedredstone.defaultcomponents.data;

import com.awakenedredstone.defaultcomponents.mixin.TagEntryAccessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*? if >=1.21.2 {*/
import net.minecraft.resource.ResourceFinder;
import net.minecraft.registry.tag.TagGroupLoader;
/*?} else {*/
/*import com.google.gson.JsonElement;
import net.minecraft.registry.tag.TagManagerLoader;
import com.awakenedredstone.defaultcomponents.mixin.DataPackContentsAccessor;
*//*?}*/

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultComponentLoader extends /*? if >=1.21.2 {*/JsonDataLoader<DefaultComponentLoader.ComponentManipulation>/*?} else {*//*JsonDataLoader*//*?}*/ implements IdentifiableResourceReloadListener {
    public static final Logger LOGGER = LoggerFactory.getLogger("Default Components Data Parser");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    /*? if <1.21.2 {*/
    /*private final RegistryOps<JsonElement> ops;
    *//*?}*/

    public DefaultComponentLoader(RegistryWrapper.WrapperLookup registryWrapper) {
        /*? if >=1.21.4 {*/
        super(registryWrapper.getOps(JsonOps.INSTANCE), ComponentManipulation.CODEC, ResourceFinder.json("default_components"));
        /*?} else if >=1.21.2 {*/
        /*super(registryWrapper.getOps(JsonOps.INSTANCE), ComponentManipulation.CODEC, "default_components");
        *//*?} else {*/
        /*super(GSON, "default_components");
        this.ops = registryWrapper.getOps(JsonOps.INSTANCE);
        *//*?}*/
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("default_components", "default_components");
    }

    //? if >=1.21.2 {
    @Override
    protected void apply(Map<Identifier, ComponentManipulation> prepared, ResourceManager manager, Profiler profiler) {
        compute(prepared, manager, profiler);
    }
    //?} else {
    /*@Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, ComponentManipulation> newPrepared = HashMap.newHashMap(prepared.size());

        prepared.forEach((identifier, jsonElement) -> {
            ComponentManipulation component = ComponentManipulation.CODEC.parse(ops, jsonElement).getOrThrow();
            newPrepared.put(identifier, component);
        });

        compute(newPrepared, manager, profiler);
    }
    *///?}

    protected void compute(Map<Identifier, ComponentManipulation> prepared, ResourceManager manager, Profiler profiler) {
        profiler.push("Default Components data loading");
        final Map<Identifier, ComponentManipulation> perItem = HashMap.newHashMap(0);
        final Map<String, ComponentManipulation> global = HashMap.newHashMap(0);
        final List<Identifier> tags = new ArrayList<>(0);

        profiler.push("Default Components data loading (tag gathering)");
        /*? if <=1.21.1 {*/
        /*var perhapsTagMap = ((DataPackContentsAccessor) DefaultComponentData.INSTANCE.dataPackContents)
          .getRegistryTagManager().getRegistryTags().stream()
          .filter(registryTags -> registryTags.key().equals(RegistryKeys.ITEM))
          .map(TagManagerLoader.RegistryTags::tags).findFirst();

        if (perhapsTagMap.isEmpty()) {
            throw new IllegalStateException("Failed to get tag data, unable modify components for the tag items");
        }

        //noinspection unchecked,rawtypes
        Map<Identifier, Collection<RegistryEntry<Item>>> tagMap = (Map) perhapsTagMap.get();
        *//*?} else {*/
        var perhapsTagMap = DefaultComponentData.INSTANCE.registryTags.stream()
          .filter(registryTags -> registryTags.key().equals(RegistryKeys.ITEM))
          .map(TagGroupLoader.RegistryTags::tags).findFirst();

        if (perhapsTagMap.isEmpty()) {
            throw new IllegalStateException("Failed to get tag data, unable modify components for the tag items");
        }

        //noinspection unchecked,rawtypes
        Map<TagKey<?>, Collection<RegistryEntry<Item>>> tagMap = (Map) perhapsTagMap.get();
        /*?}*/
        profiler.pop();

        profiler.push("Default Components data loading (entry sorting)");
        List<Map.Entry<Identifier, ComponentManipulation>> entries = new ArrayList<>(prepared.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        entries.sort(Map.Entry.comparingByValue());
        profiler.pop();

        profiler.push("Default Components data loading (map building)");
        for (Map.Entry<Identifier, ComponentManipulation> change : entries) {
            Identifier identifier = change.getKey();
            ComponentManipulation componentMap = change.getValue();

            if (componentMap.target().isPresent()) {
                Either<WildcardEntry, TagEntry> entryEither = componentMap.target().get();
                entryEither.map(wildcardEntry -> {
                    String id = wildcardEntry.modId();
                    if (global.containsKey(id)) {
                        throw new UnsupportedOperationException("Tried to register wildcard component override " + wildcardEntry.getWildcard() + " twice!");
                    }

                    global.compute(id, (ignored, componentManipulation) -> componentMap.merge(componentManipulation));

                    return null;
                }, tagEntry -> {
                    TagEntryAccessor accessor = (TagEntryAccessor) tagEntry;
                    TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, accessor.getId());

                    if (accessor.isTag()) {
                        if (tags.contains(tagKey.id())) {
                            throw new UnsupportedOperationException("Tried to register tag component override " + tagKey.id() + " twice!");
                        }

                        profiler.push("Default Components data loading (going trough tag items)");
                        /*? if >=1.21.2 {*/
                        Optional<Collection<RegistryEntry<Item>>> optional = Optional.ofNullable(tagMap.get(tagKey));
                        /*?} else {*/
                        /*Optional<Collection<RegistryEntry<Item>>> optional = Optional.ofNullable(tagMap.get(tagKey.id()));
                         *//*?}*/
                        if (optional.isPresent()) {
                            for (RegistryEntry<Item> entry : optional.get()) {
                                Optional<RegistryKey<Item>> entryKey = entry.getKey();
                                entryKey.ifPresent(itemRegistryKey -> {
                                    perItem.compute(itemRegistryKey.getValue(), (ignored, componentManipulation) -> componentMap.merge(componentManipulation));
                                    tags.add(tagKey.id());
                                });
                            }
                        } else {
                            LOGGER.warn("Failed to find tag {}, skipping", tagKey.id());
                        }
                        profiler.pop();
                    } else {
                        perItem.compute(tagKey.id(), (ignored, componentManipulation) -> componentMap.merge(componentManipulation));
                    }

                    return null;
                });
            } else {
                if (Registries.ITEM.containsId(identifier)) {
                    perItem.compute(identifier, (ignored, componentManipulation) -> componentMap.merge(componentManipulation));
                } else {
                    LOGGER.warn("Skipping invalid item {}", identifier);
                }
            }
        }
        profiler.pop();

        // I don't need this anymore, and I don't want to keep it in memory doing nothing
        /*? if >=1.21.2 {*/
        DefaultComponentData.INSTANCE.registryTags.clear();
         /*?} else {*/
        /*DefaultComponentData.INSTANCE.dataPackContents = null;
        *//*?}*/

        DefaultComponentData.INSTANCE.itemComponents = Map.copyOf(perItem);
        DefaultComponentData.INSTANCE.modComponents = Map.copyOf(global);
        profiler.push("Default Components data loading (applying)");
        DefaultComponentData.INSTANCE.modifyItems();
        profiler.pop();
        profiler.pop();
    }

    public record ComponentManipulation(Optional<Either<WildcardEntry, TagEntry>> target, Optional<Map<ComponentType<?>, Object>> additions, Optional<List<ComponentType<?>>> removals) implements Comparable<ComponentManipulation> {
        public static final ComponentManipulation EMPTY = new ComponentManipulation(Optional.empty(), Optional.empty(), Optional.empty());
        public static final Codec<ComponentType<?>> COMPONENT_CODEC = Codec.STRING
          .flatXmap(
            id -> {
                Identifier identifier = Identifier.tryParse(id);
                ComponentType<?> componentType = Registries.DATA_COMPONENT_TYPE.get(identifier);
                if (componentType == null) {
                    return DataResult.error(() -> "No component with type: '" + identifier + "'");
                } else {
                    return componentType.shouldSkipSerialization()
                      ? DataResult.error(() -> "'" + identifier + "' is not a persistent component")
                      : DataResult.success(componentType);
                }
            },
            type -> {
                Identifier identifier = Registries.DATA_COMPONENT_TYPE.getId(type);
                return identifier == null
                  ? DataResult.error(() -> "Unregistered component: " + type)
                  : DataResult.success(identifier.toString());
            }
          );
        @SuppressWarnings("unchecked")
        public static final Codec<ComponentManipulation> CODEC = RecordCodecBuilder.create(instance ->
          instance.group(
            Codec.either(WildcardEntry.CODEC, TagEntry.CODEC).optionalFieldOf("target").forGetter(ComponentManipulation::target),
            Codec.dispatchedMap(COMPONENT_CODEC, component -> (Codec<Object>) component.getCodecOrThrow()).optionalFieldOf("add").forGetter(ComponentManipulation::additions),
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

                return new DefaultComponentLoader.ComponentManipulation(Optional.empty(), add, remove);
            }

            private static <T> void encodeComponent(RegistryByteBuf buf, ComponentType<T> type, Object value) {
                //noinspection unchecked
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

        public ComponentManipulation merge(@Nullable ComponentManipulation other) {
            if (other == null) return this;

            final Map<ComponentType<?>, Object> newAdditions = other.additions().map(HashMap::new).orElse(HashMap.newHashMap(0));
            final List<ComponentType<?>> newRemovals = other.removals().map(ArrayList::new).orElse(new ArrayList<>());

            additions.ifPresent(newAdditions::putAll);
            removals.ifPresent(newRemovals::addAll);

            return new ComponentManipulation(Optional.empty(), Optional.of(Map.copyOf(newAdditions)), Optional.of(List.copyOf(newRemovals)));
        }

        @Override
        public int compareTo(@NotNull ComponentManipulation o) {
            if (this.target().isEmpty() && o.target().isPresent()) return 1;
            if (this.target().isPresent() && o.target().isEmpty()) return -1;

            if (this.target().isPresent()) {
                if (target().get().left().isPresent() && o.target().get().right().isPresent()) {
                    return -1;
                }
                if (target().get().right().isPresent() && o.target().get().left().isPresent()) {
                    return 1;
                }
            }

            return 0;
        }
    }

    public record WildcardEntry(String modId) {
        public static final Codec<WildcardEntry> CODEC = Codec.STRING.comapFlatMap(wildcard -> {
            try {
                return DataResult.success(splitOn(wildcard, ':'));
            } catch (InvalidIdentifierException e) {
                return DataResult.error(() -> "Not a valid wildcard: " + wildcard + " " + e.getMessage());
            }
        }, WildcardEntry::getWildcard).stable();

        public static WildcardEntry splitOn(String id, char delimiter) {
            int i = id.indexOf(delimiter);
            if (i > 0) {
                String modId = id.substring(0, i);
                String wildcard = id.substring(i + 1);
                if (wildcard.equals("*")) {
                    if (Identifier.isNamespaceValid(modId)) {
                        return new WildcardEntry(id.substring(0, i));
                    } else {
                        throw new InvalidIdentifierException("Non [a-z0-9_.-] character in namespace of location: " + modId + ":" + wildcard);
                    }
                } else {
                    throw new InvalidIdentifierException("Non wildcard [*] character in path of location: " + modId + ":" + wildcard);
                }
            } else if (id.equals("*")) {
                return new WildcardEntry(id);
            } else {
                throw new InvalidIdentifierException("Non wildcard [*] character in path of location: " + id);
            }
        }

        public boolean isGeneric() {
            return modId.equals("*");
        }

        public String getWildcard() {
            return isGeneric() ? modId : modId + ":" + "*";
        }
    }
}
