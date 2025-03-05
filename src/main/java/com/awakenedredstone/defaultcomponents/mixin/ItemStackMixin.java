package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentData;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import com.awakenedredstone.defaultcomponents.duck.RebuildDefaultComponents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(value = ItemStack.class, priority = 990)
public abstract class ItemStackMixin implements RebuildDefaultComponents {

    @Mixin(targets = "net.minecraft.item.ItemStack$1")
    private static class PacketCodec {
        //? if >=1.21.2 {
        @ModifyExpressionValue(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/MergedComponentMap;getChanges()Lnet/minecraft/component/ComponentChanges;"))
        /*?} else {*/
        /*@ModifyExpressionValue(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/ComponentMapImpl;getChanges()Lnet/minecraft/component/ComponentChanges;"))
        *//*?}*/
        private ComponentChanges informVanilla(ComponentChanges original, @Local(argsOnly = true) ItemStack stack) {
            PacketContext context = PacketContext.get();
            if (context == null || context.getPlayer() == null || context.getGameProfile() == null) {
                return original;
            }

            if (!DefaultComponents.MODDED_PLAYERS.contains(context.getGameProfile())) {
                ComponentChanges.Builder builder = ComponentChanges.builder();
                Identifier id = Registries.ITEM.getId(stack.getItem());

                DefaultComponentLoader.ComponentManipulation global = DefaultComponentData.INSTANCE.getModComponents().get("*");
                if (global != null) {
                    global.forEachRemoved(builder::remove);
                    global.forEachAdded(builder::add);
                }

                DefaultComponentLoader.ComponentManipulation modManipulation = DefaultComponentData.INSTANCE.getModComponents().get(id.getNamespace());
                if (modManipulation != null) {
                    modManipulation.forEachRemoved(builder::remove);
                    modManipulation.forEachAdded(builder::add);
                }

                DefaultComponentLoader.ComponentManipulation manipulation = DefaultComponentData.INSTANCE.getItemComponents().get(id);
                if (manipulation != null) {
                    manipulation.forEachRemoved(builder::remove);
                    manipulation.forEachAdded(builder::add);
                }

                ComponentChanges.AddedRemovedPair pair = original.toAddedRemovedPair();
                pair.removed().forEach(builder::remove);
                pair.added().forEach(builder::add);

                return builder.build();
            }

            return original;
        }
    }
}
