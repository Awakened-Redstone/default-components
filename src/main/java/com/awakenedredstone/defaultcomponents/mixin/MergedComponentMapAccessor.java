package com.awakenedredstone.defaultcomponents.mixin;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MergedComponentMap.class)
public interface MergedComponentMapAccessor {
    @Accessor void setBaseComponents(ComponentMap map);
    @Accessor ComponentMap getBaseComponents();
}
