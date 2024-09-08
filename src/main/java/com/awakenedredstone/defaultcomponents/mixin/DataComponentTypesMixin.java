package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(DataComponentTypes.class)
public class DataComponentTypesMixin {

    @WrapOperation(method = "<clinit>",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/component/ComponentMap$Builder;build()Lnet/minecraft/component/ComponentMap;"),
      slice = @Slice(
        from = @At(value = "FIELD", target = "Lnet/minecraft/component/DataComponentTypes;DEFAULT_ITEM_COMPONENTS:Lnet/minecraft/component/ComponentMap;", shift = At.Shift.BEFORE),
        to = @At(value = "FIELD", target = "Lnet/minecraft/component/DataComponentTypes;DEFAULT_ITEM_COMPONENTS:Lnet/minecraft/component/ComponentMap;", shift = At.Shift.AFTER)
      ),
      require = 1)
    private static ComponentMap addGlobalComponents(ComponentMap.Builder instance, Operation<ComponentMap> original) {
        DefaultComponents.CONFIG.global.forEach((componentType, object) -> {
            //noinspection unchecked
            instance.add((ComponentType<Object>) componentType, object);
        });
        return original.call(instance);
    }
}
