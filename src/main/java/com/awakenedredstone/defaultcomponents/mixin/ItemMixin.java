package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.duck.SillyItemThing;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Item.class)
public class ItemMixin implements SillyItemThing {
    @Shadow @Mutable @Final private ComponentMap components;

    @Override
    public void defaultComponents$addToDefaultComponents(Map<ComponentType<?>, Object> components) {
        ComponentMap.Builder builder = ComponentMap.builder();
        for (Component<?> component : this.components) {
            builder.add((ComponentType<Object>) component.type(), component.value());
        }

        components.forEach((componentType, value) -> {
            builder.add((ComponentType<Object>) componentType, value);
        });

        this.components = builder.build();
    }
}
