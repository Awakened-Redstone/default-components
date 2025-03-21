package com.awakenedredstone.defaultcomponents.mixin;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.DataPackContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void getCurrentContents(CallbackInfo ci) {
        DefaultComponentData.INSTANCE.dataPackContents = (DataPackContents) (Object) this;
    }

    @ModifyExpressionValue(method = "method_58296", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;isDebugEnabled()Z"))
    private static boolean enableDebug(boolean original) {
        return original || Boolean.getBoolean("defaultcomponents.debug.reload");
    }
}
