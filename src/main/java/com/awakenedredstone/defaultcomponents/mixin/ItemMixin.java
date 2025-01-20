package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import com.awakenedredstone.defaultcomponents.duck.ModifyDefaultComponents;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import org.objectweb.asm.Opcodes;
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
    public void defaultComponents$modifyComponents(DefaultComponentLoader.ComponentManipulation components) {
        ComponentMap.Builder builder = ComponentMap.builder();

        DefaultComponentLoader.ComponentManipulation globalComponents = DefaultComponentLoader.INSTANCE.getGlobalComponents();
        for (Component<?> component : defaultComponents) {
            if (globalComponents.isRemoved(component.type()) || (components != null && components.isRemoved(component.type()))) {
                continue;
            }

            builder.add((ComponentType<Object>) component.type(), component.value());
        }

        globalComponents.forEachAdded(builder::add);

        if (components != null) {
            components.forEachAdded(builder::add);
        }

        this.components = builder.build();
    }

    @Override
    public ComponentMap defaultComponents$defaultComponents() {
        return defaultComponents;
    }
}
