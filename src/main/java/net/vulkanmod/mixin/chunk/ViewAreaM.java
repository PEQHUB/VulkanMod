package net.vulkanmod.mixin.chunk;

import net.minecraft.class_769;
import net.minecraft.class_846;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_769.class)
public abstract class ViewAreaM {

	@Shadow public class_846.class_851[] sections;

	@Shadow protected abstract void setViewDistance(int i);

	@Inject(method = "createSections", at = @At("HEAD"))
	private void skipAllocation(class_846 sectionRenderDispatcher, CallbackInfo ci) {
		// It's not possible to completely skip allocation since it would cause an error if repositionCamera is called
		this.setViewDistance(0);
	}
}
