package com.awakenedredstone.defaultcomponents.mixin;

import net.minecraft.component.ComponentMap;
//? if >=1.21.2 {
import net.minecraft.component.MergedComponentMap;
//?} else {
/*import net.minecraft.component.ComponentMapImpl;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=1.21.2 {
@Mixin(MergedComponentMap.class)
//?} else {
/*@Mixin(ComponentMapImpl.class)
*///?}
public interface MergedComponentMapAccessor {
    @Accessor void setBaseComponents(ComponentMap map);
    @Accessor ComponentMap getBaseComponents();
}
