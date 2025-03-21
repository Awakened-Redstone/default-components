package com.awakenedredstone.defaultcomponents.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerSystem;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProfilerSystem.class)
public class ProfilerSystemMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private String fullPath;

    @Inject(method = "pop", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(JJ)J"))
    private void logDebug(CallbackInfo ci, @Local(ordinal = 2) long time) {
        if (Boolean.getBoolean("defaultcomponents.debug.reload")) {
            LOGGER.info(
              "'{}' took aprox {} ms",
              LogUtils.defer(() -> ProfileResult.getHumanReadableName(this.fullPath)),
              LogUtils.defer(() -> (double) time / 1000000.0)
            );
        }
    }
}
