package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentData;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import com.awakenedredstone.defaultcomponents.duck.RebuildDefaultComponents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentChanges;
//? if >=1.21.2 {
import net.minecraft.component.MergedComponentMap;
//?} else {
/*import net.minecraft.component.ComponentMapImpl;
*///?}
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements RebuildDefaultComponents {
    /*? if >=1.21.2 {*/
    @Shadow @Final @Mutable MergedComponentMap components;
    /*?} else {*/
    /*@Shadow @Final @Mutable ComponentMapImpl components;
    *//*?}*/
    @Shadow public abstract Item getItem();

    @Override
    public void defaultComponents$rebuildComponents() {
        ((MergedComponentMapAccessor) (Object) components).setBaseComponents(getItem().getComponents());
    }

    /*? if >=1.21.2 {*/
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At("TAIL"))
    private void track(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        DefaultComponents.ITEM_STACKS.add(this);
    }
    /*?} else {*/
    /*@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At("TAIL"))
    private void track(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
        DefaultComponents.ITEM_STACKS.add(this);
    }
    *//*?}*/

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
