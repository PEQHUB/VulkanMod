package net.vulkanmod.mixin.debug;

import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_309.class)
public abstract class KeyboardHandlerM {

    @Shadow protected abstract boolean handleChunkDebugKeys(class_11908 keyEvent);

    @Shadow private boolean usedDebugKeyAsModifier;

    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", ordinal = 1))
    private void chunkDebug(long l, int i, class_11908 keyEvent, CallbackInfo ci) {
        // GLFW key 296 -> F7
        // U -> Capture frustum
        this.usedDebugKeyAsModifier |= class_3675.method_15987(class_310.method_1551().method_22683(), 296)
                && this.handleChunkDebugKeys(keyEvent);
    }
}
