package com.awakenedredstone.defaultcomponents.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.JsonElement;
import com.awakenedredstone.defaultcomponents.config.source.ConfigHandler;
import com.awakenedredstone.defaultcomponents.config.source.jankson.JanksonOps;
import com.awakenedredstone.defaultcomponents.util.JanksonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DefaultComponentsConfig extends ConfigHandler {
    private final Codec<DefaultComponentsConfig> CODEC = RecordCodecBuilder.create(instance ->
      instance.group(
        ComponentType.TYPE_TO_VALUE_MAP_CODEC.fieldOf("global").forGetter(c -> c.global),
        Codec.unboundedMap(Identifier.CODEC, ComponentType.TYPE_TO_VALUE_MAP_CODEC).fieldOf("perItem").forGetter(c -> c.perItem)
      ).apply(instance, (global, perItem) -> {
          this.global = global;
          this.perItem = perItem;
          return this;
      })
    );

    public DefaultComponentsConfig() {
        super("default_components", JanksonBuilder.buildJankson());
    }

    @Comment("Components that will apply to all items [REQUIRES RESTART]")
    public Map<ComponentType<?>, Object> global = new HashMap<>();

    @Comment("Components that will apply to specific items [REQUIRES RESTART]")
    public Map<Identifier, Map<ComponentType<?>, Object>> perItem = new HashMap<>();

    @Override
    public void load() {
        if (!configExists()) {
            this.save();
            return;
        }

        try {
            this.loading = true;
            JsonElement configContent = getInterpreter().fromJson(Files.readString(this.getFileLocation(), StandardCharsets.UTF_8), JsonElement.class);

            CODEC.parse(JanksonOps.INSTANCE, configContent);

            LOGGER.info("Config loaded");
        } catch (Throwable e) {
            LOGGER.error("Could not load config!", e);
            CrashReport.create(e, "Failed to load the \"Default Components\" config, crashing to avoid data corruption");
        } finally {
            this.loading = false;
        }
    }
}
