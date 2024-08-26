package com.hazzabro124.marionetta.forge.mixin.server;

import com.hazzabro124.marionetta.TickScheduler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(value = MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(
            method = "tickServer(Ljava/util/function/BooleanSupplier;)V",
            at = @At("TAIL")
    )
    private void tickServer(BooleanSupplier hasMoreTime, CallbackInfo ci) {
        TickScheduler.INSTANCE.tickServer((MinecraftServer) (Object) this);
    }
}
