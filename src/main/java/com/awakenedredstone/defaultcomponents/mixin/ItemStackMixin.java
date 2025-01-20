package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import com.awakenedredstone.defaultcomponents.duck.RebuildDefaultComponents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements RebuildDefaultComponents {
    @Shadow @Final @Mutable MergedComponentMap components;
    @Shadow public abstract Item getItem();

    @Override
    public void defaultComponents$rebuildComponents() {
        ((MergedComponentMapAccessor) (Object) components).setBaseComponents(getItem().getComponents());
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At("TAIL"))
    private void track(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        DefaultComponents.ITEM_STACKS.add(this);
    }

    @Mixin(targets = "net.minecraft.item.ItemStack$1")
    private static class PacketCodec {
        @ModifyExpressionValue(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/MergedComponentMap;getChanges()Lnet/minecraft/component/ComponentChanges;"))
        private ComponentChanges informVanilla(ComponentChanges original, @Local(argsOnly = true) ItemStack stack) {
            PacketContext context = PacketContext.get();
            if (context == null || context.getPlayer() == null || context.getGameProfile() == null) {
                return original;
            }

            if (!DefaultComponents.MODDED_PLAYERS.contains(context.getGameProfile())) {
                ComponentChanges.Builder builder = ComponentChanges.builder();
                Identifier id = Registries.ITEM.getId(stack.getItem());

                DefaultComponentLoader.INSTANCE.getGlobalComponents().forEachRemoved(builder::remove);
                DefaultComponentLoader.INSTANCE.getGlobalComponents().forEachAdded(builder::add);

                DefaultComponentLoader.ComponentManipulation manipulation = DefaultComponentLoader.INSTANCE.getItemComponents().get(id);
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

    /*@ModifyReturnValue(method = "getComponents", at = @At("RETURN"))
    private ComponentMap patchComponents(ComponentMap original) {
        if (NetworkUtil.isServerNetworkingThread() && this.getItem() instanceof ModifyDefaultComponents item) {
            Identifier id = Registries.ITEM.getId(this.getItem());
            DefaultComponentLoader.ComponentManipulation components = DefaultComponentLoader.INSTANCE.getItemComponents().get(id);
            ComponentMap.Builder builder = ComponentMap.builder();

            DefaultComponentLoader.ComponentManipulation globalComponents = DefaultComponentLoader.INSTANCE.getGlobalComponents();
            for (Component<?> component : item.defaultComponents$defaultComponents()) {
                if (globalComponents.isRemoved(component.type()) || (components != null && components.isRemoved(component.type()))) {
                    continue;
                }

                builder.add((ComponentType<Object>) component.type(), component.value());
            }

            globalComponents.forEachAdded((type, value) -> builder.add((ComponentType<Object>) type, value));

            if (components != null) {
                components.forEachAdded((componentType, value) -> {
                    builder.add((ComponentType<Object>) componentType, value);
                });
            }

            for (Component<?> component : original) {
                if (globalComponents.isRemoved(component.type()) || (components != null && components.isRemoved(component.type()))) {
                    continue;
                }

                builder.add((ComponentType<Object>) component.type(), component.value());
            }

            return new ComponentMapImpl(builder.build());
        }

        return original;
    }*/
}
