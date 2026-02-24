package net.vulkanmod.mixin.render.frapi;

import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_918.class)
public interface ItemRendererAccessor {
	@Invoker("getSpecialFoilBuffer")
	static class_4588 getSpecialFoilBuffer(class_4597 provider, class_1921 layer, class_4587.class_4665 entry) {
		throw new AssertionError();
	}
}
