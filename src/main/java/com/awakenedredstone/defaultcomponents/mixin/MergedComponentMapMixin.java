package com.awakenedredstone.defaultcomponents.mixin;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MergedComponentMap.class)
public class MergedComponentMapMixin {
    @Shadow @Final @Mutable private ComponentMap baseComponents;
}
