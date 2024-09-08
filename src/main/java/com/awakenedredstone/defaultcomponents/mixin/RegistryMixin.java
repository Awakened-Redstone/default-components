package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.awakenedredstone.defaultcomponents.duck.SillyItemThing;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Registry.class)
public interface RegistryMixin {

    @Inject(method = "register(Lnet/minecraft/registry/Registry;Lnet/minecraft/registry/RegistryKey;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("TAIL"))
    private static <V, T> void addItemComponents(Registry<V> registry, RegistryKey<V> key, T entry, CallbackInfoReturnable<T> cir) {
        if (registry == Registries.ITEM && entry instanceof SillyItemThing item) {
            Map<ComponentType<?>, Object> components = DefaultComponents.CONFIG.perItem.get(key.getValue());
            if (components != null) {
                item.defaultComponents$addToDefaultComponents(components);
            }
        }
    }
}
