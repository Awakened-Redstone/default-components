package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentData;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import com.awakenedredstone.defaultcomponents.duck.ModifyDefaultComponents;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin implements ModifyDefaultComponents {
    @Shadow @Mutable @Final private ComponentMap components;
    @Unique private ComponentMap defaultComponents;

    @Inject(method = "<init>", at = @At("TAIL"), require = 1)
    private void storeInitialDefault(CallbackInfo ci) {
        defaultComponents = components;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void defaultComponents$modifyComponents(DefaultComponentLoader.ComponentManipulation itemComponents, DefaultComponentLoader.ComponentManipulation modComponents) {
        ComponentMap.Builder builder = ComponentMap.builder();

        DefaultComponentLoader.ComponentManipulation globalComponents = DefaultComponentData.INSTANCE.getModComponents().getOrDefault("*", DefaultComponentLoader.ComponentManipulation.EMPTY);
        for (Component<?> component : defaultComponents) {
            if (globalComponents.isRemoved(component.type()) || (modComponents != null && modComponents.isRemoved(component.type())) || (itemComponents != null && itemComponents.isRemoved(component.type()))) {
                continue;
            }

            builder.add((ComponentType<Object>) component.type(), component.value());
        }

        globalComponents.forEachAdded(builder::add);

        if (modComponents != null) {
            modComponents.forEachAdded(builder::add);
        }

        if (itemComponents != null) {
            itemComponents.forEachAdded(builder::add);
        }

        this.components = builder.build();
    }

    @Override
    public ComponentMap defaultComponents$defaultComponents() {
        return defaultComponents;
    }
}
