package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentData;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/*? if >=1.21.2 {*/
@Mixin(TagGroupLoader.class)
/*?}*/
public class TagGroupLoaderMixin {
    /*? if >=1.21.2 {*/
    @Inject(method = "startReload(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/DynamicRegistryManager;)Ljava/util/List;", at = @At("HEAD"))
    private static void clearList(ResourceManager resourceManager, DynamicRegistryManager registryManager, CallbackInfoReturnable<List<Registry.PendingTagLoad<?>>> cir) {
        DefaultComponentData.INSTANCE.registryTags.clear();
    }

    @Inject(method = "startReload(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/Registry;)Ljava/util/Optional;", at = @At("TAIL"))
    private static void recordTag(ResourceManager resourceManager, Registry<?> registry, CallbackInfoReturnable<Optional<Registry.PendingTagLoad<?>>> cir, @Local TagGroupLoader.RegistryTags<?> registryTags) {
        DefaultComponentData.INSTANCE.registryTags.add(registryTags);
    }
    /*?}*/
}
