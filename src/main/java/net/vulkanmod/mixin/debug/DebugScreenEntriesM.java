package net.vulkanmod.mixin.debug;

import net.minecraft.class_11631;
import net.minecraft.class_11632;
import net.minecraft.class_2960;
import net.vulkanmod.render.profiling.DebugEntryMemoryStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_11631.class)
public abstract class DebugScreenEntriesM {

    @Shadow
    public static class_2960 register(class_2960 resourceLocation, class_11632 debugScreenEntry) {
        return null;
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addEntry(CallbackInfo ci) {
        register(class_2960.method_60655("vkmod","stats"), new DebugEntryMemoryStats());
    }
}
