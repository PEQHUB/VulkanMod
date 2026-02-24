package net.vulkanmod.render.chunk.build.frapi.render;

import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.class_1087;
import net.minecraft.class_11515;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4696;
import net.minecraft.class_5819;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableQuadViewImpl;
import net.vulkanmod.render.chunk.build.light.LightMode;
import net.vulkanmod.render.chunk.build.light.LightPipeline;
import net.vulkanmod.render.chunk.build.light.data.ArrayLightDataCache;
import net.vulkanmod.render.chunk.build.light.flat.FlatLightPipeline;
import net.vulkanmod.render.chunk.build.light.smooth.NewSmoothLightPipeline;
import net.vulkanmod.render.chunk.build.light.smooth.SmoothLightPipeline;

/**
 * Context for non-terrain block rendering.
 */
public class BlockRenderContext extends AbstractBlockRenderContext {
	public static final ThreadLocal<BlockRenderContext> POOL = ThreadLocal.withInitial(BlockRenderContext::new);

	private BlockVertexConsumerProvider vertexConsumers;
    private class_11515 defaultRenderLayer;

	private final ArrayLightDataCache lightDataCache = new ArrayLightDataCache();

	public BlockRenderContext() {
		LightPipeline flatLightPipeline = new FlatLightPipeline(this.lightDataCache);

		LightPipeline smoothLightPipeline;
		if (Initializer.CONFIG.ambientOcclusion == LightMode.SUB_BLOCK) {
			smoothLightPipeline = new NewSmoothLightPipeline(lightDataCache);
		}
		else {
			smoothLightPipeline = new SmoothLightPipeline(lightDataCache);
		}

		this.setupLightPipelines(flatLightPipeline, smoothLightPipeline);

		random = class_5819.method_43047();
    }

	public void render(class_1920 blockView, class_1087 model, class_2680 state, class_2338 pos, class_4587 matrixStack, BlockVertexConsumerProvider buffers, boolean cull, long seed, int overlay) {
		class_243 offset = state.method_26226(pos);
		matrixStack.method_22904(offset.field_1352, offset.field_1351, offset.field_1350);

		this.blockPos = pos;
		this.vertexConsumers = buffers;
        this.defaultRenderLayer = class_4696.method_23679(state);
		this.matrices = matrixStack.method_23760();
		this.overlay = overlay;
		this.random.method_43052(seed);

		this.lightDataCache.reset(blockView, pos);

		this.prepareForWorld(blockView, cull);
		this.prepareForBlock(state, pos, state.method_26213() == 0);

		model.emitQuads(getEmitter(), blockView, pos, state, random, this::isFaceCulled);

		this.vertexConsumers = null;
	}

	@Override
	protected class_4588 getVertexConsumer(class_11515 layer) {
		return vertexConsumers.getBuffer(layer);
	}

	protected void endRenderQuad(MutableQuadViewImpl quad) {
		final TriState aoMode = quad.ambientOcclusion();
		final boolean ao = this.useAO && (aoMode == TriState.TRUE || (aoMode == TriState.DEFAULT && this.defaultAO));
		final boolean emissive = quad.emissive();
		final boolean vanillaShade = quad.shadeMode() == ShadeMode.VANILLA;
        final class_11515 quadRenderLayer = quad.renderLayer();
        final class_11515 renderLayer = quadRenderLayer == null ? defaultRenderLayer : quadRenderLayer;
		final class_4588 vertexConsumer = getVertexConsumer(renderLayer);

		LightPipeline lightPipeline = ao ? this.smoothLightPipeline : this.flatLightPipeline;

		tintQuad(quad);
		shadeQuad(quad, lightPipeline, emissive, vanillaShade);
		copyLightData(quad);
        bufferQuad(quad, vertexConsumer);
	}

	private void copyLightData(MutableQuadViewImpl quad) {
        for (int i = 0; i < 4; i++) {
			quad.lightmap(i, this.quadLightData.lm[i]);
		}
	}

}
