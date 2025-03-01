package com.awakenedredstone.defaultcomponents.mixin;

/*? >=1.21.2 {*/
import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MergedComponentMap.class)
public class MergedComponentMapMixin {
    // Allows accessor to not crash the game, do not remove
    @Shadow @Final @Mutable private ComponentMap baseComponents;
}
/*?}*/
