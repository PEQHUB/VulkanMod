package net.vulkanmod.render.chunk.build.frapi;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.fabricmc.fabric.mixin.client.indigo.renderer.BlockRenderDispatcherAccessor;
import net.minecraft.class_10444;
import net.minecraft.class_1087;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2464;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_776;
import net.minecraft.class_778;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessLayerRenderState;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableMeshImpl;
import net.vulkanmod.render.chunk.build.frapi.render.BlockRenderContext;
import net.vulkanmod.render.chunk.build.frapi.render.SimpleBlockRenderContext;

/**
 * Fabric renderer implementation.
 */
public class VulkanModRenderer implements Renderer {
	public static final VulkanModRenderer INSTANCE = new VulkanModRenderer();

	private VulkanModRenderer() {}

	@Override
	public MutableMesh mutableMesh() {
		return new MutableMeshImpl();
	}

	@Override
	public void render(class_778 modelBlockRenderer, class_1920 blockAndTintGetter,
					   class_1087 blockStateModel, class_2680 blockState, class_2338 blockPos, class_4587 poseStack,
					   BlockVertexConsumerProvider blockVertexConsumerProvider, boolean cull, long seed, int overlay) {
		BlockRenderContext.POOL.get().render(blockAndTintGetter, blockStateModel, blockState, blockPos, poseStack, blockVertexConsumerProvider, cull, seed, overlay);
	}

	@Override
	public void render(class_4587.class_4665 pose, BlockVertexConsumerProvider blockVertexConsumerProvider, class_1087 blockStateModel,
					   float v, float v1, float v2, int i, int i1, class_1920 blockAndTintGetter,
					   class_2338 blockPos, class_2680 blockState) {
		SimpleBlockRenderContext.POOL.get().bufferModel(pose, blockVertexConsumerProvider, blockStateModel, v, v1, v2, i, i1, blockAndTintGetter, blockPos, blockState);
	}

	@Override
	public void renderBlockAsEntity(class_776 blockRenderDispatcher, class_2680 state, class_4587 poseStack, class_4597 bufferSource, int light, int overlay, class_1920 blockView, class_2338 pos) {
		class_2464 blockRenderType = state.method_26217();

		if (blockRenderType != class_2464.field_11455) {
			class_1087 model = blockRenderDispatcher.method_3349(state);
			int tint = ((BlockRenderDispatcherAccessor) blockRenderDispatcher).getBlockColors().method_1697(state, null, null, 0);
			float red = (tint >> 16 & 255) / 255.0F;
			float green = (tint >> 8 & 255) / 255.0F;
			float blue = (tint & 255) / 255.0F;
			FabricBlockModelRenderer.render(poseStack.method_23760(), RenderLayerHelper.entityDelegate(bufferSource), model, red, green, blue, light, overlay, blockView, pos, state);
		}
	}

	@Override
	public QuadEmitter getLayerRenderStateEmitter(class_10444.class_10446 layer) {
		return ((AccessLayerRenderState) layer).getMutableMesh().emitter();
	}

	@Override
	public void setLayerRenderTypeGetter(class_10444.class_10446 layer,
										 ItemRenderTypeGetter renderTypeGetter) {
		((AccessLayerRenderState) layer).setRenderTypeGetter(renderTypeGetter);
	}
}
