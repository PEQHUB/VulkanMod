package net.vulkanmod.mixin.render.color;

import net.minecraft.class_2248;
import net.minecraft.class_322;
import net.minecraft.class_324;
import net.vulkanmod.interfaces.color.BlockColorsExtended;
import net.vulkanmod.render.chunk.build.color.BlockColorRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_324.class)
public class BlockColorsM implements BlockColorsExtended {

	@Unique
	private BlockColorRegistry colorResolvers = new BlockColorRegistry();

	@Inject(method = "register", at = @At("RETURN"))
	private void onRegister(class_322 blockColor, class_2248[] blocks, CallbackInfo ci) {
		this.colorResolvers.register(blockColor, blocks);
	}

	@Override
	public BlockColorRegistry getColorResolverMap() {
		return this.colorResolvers;
	}
}
