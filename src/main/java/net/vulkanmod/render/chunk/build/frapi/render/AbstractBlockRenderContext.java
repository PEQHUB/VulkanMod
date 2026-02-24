package net.vulkanmod.render.chunk.build.frapi.render;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.class_1087;
import net.minecraft.class_10889;
import net.minecraft.class_11515;
import net.minecraft.class_1920;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_247;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_322;
import net.minecraft.class_324;
import net.minecraft.class_4588;
import net.minecraft.class_4696;
import net.minecraft.class_5819;
import net.minecraft.class_765;
import net.vulkanmod.interfaces.color.BlockColorsExtended;
import net.vulkanmod.render.chunk.build.color.BlockColorRegistry;
import net.vulkanmod.render.chunk.build.frapi.VulkanModRenderer;
import net.vulkanmod.render.chunk.build.light.LightPipeline;
import net.vulkanmod.render.chunk.build.light.data.QuadLightData;
import org.jetbrains.annotations.Nullable;
import net.vulkanmod.render.chunk.build.frapi.helper.ColorHelper;
import net.vulkanmod.render.chunk.build.frapi.mesh.EncodingFormat;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableQuadViewImpl;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractBlockRenderContext extends AbstractRenderContext {
	private static final Renderer RENDERER = VulkanModRenderer.INSTANCE;

	protected final BlockColorRegistry blockColorRegistry;

	private final MutableQuadViewImpl editorQuad = new MutableQuadViewImpl() {
		{
			data = new int[EncodingFormat.TOTAL_STRIDE];
			clear();
		}

		@Override
		public void emitDirectly() {
			renderQuad(this);
		}

	};

	protected class_2680 blockState;
	protected class_2338 blockPos;
	protected class_2338.class_2339 tempPos = new class_2338.class_2339();
	protected class_11515 defaultLayer;

	protected class_1920 renderRegion;

	protected final Object2ByteLinkedOpenHashMap<ShapePairKey> occlusionCache = new Object2ByteLinkedOpenHashMap<>(2048, 0.25F) {
		protected void rehash(int i) {
		}
	};

	protected final QuadLightData quadLightData = new QuadLightData();
	protected LightPipeline smoothLightPipeline;
	protected LightPipeline flatLightPipeline;

	protected boolean useAO;
	protected boolean defaultAO;

	protected class_5819 random;

	protected boolean enableCulling = true;
	protected int cullCompletionFlags;
	protected int cullResultFlags;

	protected AbstractBlockRenderContext() {
		this.occlusionCache.defaultReturnValue((byte) 127);

		class_324 blockColors = class_310.method_1551().method_1505();
		this.blockColorRegistry = BlockColorsExtended.from(blockColors).getColorResolverMap();
	}

	protected void setupLightPipelines(LightPipeline flatLightPipeline, LightPipeline smoothLightPipeline) {
		this.flatLightPipeline = flatLightPipeline;
		this.smoothLightPipeline = smoothLightPipeline;
	}

	public void prepareForWorld(class_1920 blockView, boolean enableCulling) {
		this.renderRegion = blockView;
		this.enableCulling = enableCulling;
	}

	public void prepareForBlock(class_2680 blockState, class_2338 blockPos, boolean modelAo) {
		this.blockPos = blockPos;
		this.blockState = blockState;
		this.defaultLayer = class_4696.method_23679(blockState);

		this.useAO = class_310.method_1588();
		this.defaultAO = this.useAO && modelAo && blockState.method_26213() == 0;

		this.cullCompletionFlags = 0;
		this.cullResultFlags = 0;
	}

	public boolean isFaceCulled(@Nullable class_2350 face) {
		return !this.shouldRenderFace(face);
	}

	public boolean shouldRenderFace(class_2350 face) {
		if (face == null || !enableCulling) {
			return true;
		}

		final int mask = 1 << face.method_10146();

		if ((cullCompletionFlags & mask) == 0) {
			cullCompletionFlags |= mask;

			if (this.faceNotOccluded(blockState, face)) {
				cullResultFlags |= mask;
				return true;
			} else {
				return false;
			}
		} else {
			return (cullResultFlags & mask) != 0;
		}
	}

	public boolean faceNotOccluded(class_2680 blockState, class_2350 face) {
		class_1922 blockGetter = this.renderRegion;

		class_2338 adjPos = tempPos.method_25505(blockPos, face);
		class_2680 adjBlockState = blockGetter.method_8320(adjPos);

		if (blockState.method_26187(adjBlockState, face)) {
			return false;
		}

		if (adjBlockState.method_26225()) {
			class_265 shape = blockState.method_26173(face);

			if (shape.method_1110())
				return true;

			class_265 adjShape = adjBlockState.method_26173(face.method_10153());

			if (adjShape.method_1110())
				return true;

			if (shape == class_259.method_1077() && adjShape == class_259.method_1077()) {
				return false;
			}

			ShapePairKey blockStatePairKey = new ShapePairKey(shape, adjShape);

			byte b = occlusionCache.getAndMoveToFirst(blockStatePairKey);
			if (b != 127) {
				return b != 0;
			} else {
				boolean bl = class_259.method_1074(shape, adjShape, class_247.field_16886);

				if (occlusionCache.size() == 2048) {
					occlusionCache.removeLastByte();
				}

				occlusionCache.putAndMoveToFirst(blockStatePairKey, (byte) (bl ? 1 : 0));
				return bl;
			}
		}

		return true;
	}

	public QuadEmitter getEmitter() {
		editorQuad.clear();
		return editorQuad;
	}

	@Override
	protected void bufferQuad(MutableQuadViewImpl quadView) {
		this.renderQuad(quadView);
	}

	protected abstract class_4588 getVertexConsumer(class_11515 layer);

	private void renderQuad(MutableQuadViewImpl quad) {
		if (isFaceCulled(quad.cullFace())) {
			return;
		}

		endRenderQuad(quad);
	}

	protected void endRenderQuad(MutableQuadViewImpl quad) {}

	/** handles block color, common to all renders. */
	protected void tintQuad(MutableQuadViewImpl quad) {
		int tintIndex = quad.tintIndex();

		if (tintIndex != -1) {
			final int blockColor = getBlockColor(this.renderRegion, tintIndex);

			for (int i = 0; i < 4; i++) {
				quad.color(i, ColorHelper.multiplyColor(blockColor, quad.color(i)));
			}
		}
	}

	private int getBlockColor(class_1920 region, int colorIndex) {
		class_322 blockColor = this.blockColorRegistry.getBlockColor(this.blockState.method_26204());

		int color = blockColor != null ? blockColor.getColor(blockState, region, blockPos, colorIndex) : -1;
		return 0xFF000000 | color;
	}

	protected void shadeQuad(MutableQuadViewImpl quad, LightPipeline lightPipeline, boolean emissive, boolean vanillaShade) {
		QuadLightData data = this.quadLightData;

		// TODO: enhanced AO
		lightPipeline.calculate(quad, this.blockPos, data, quad.cullFace(), quad.lightFace(), quad.diffuseShade());

		if (emissive) {
			for (int i = 0; i < 4; i++) {
				quad.color(i, ColorHelper.multiplyRGB(quad.color(i), data.br[i]));
//				quad.lightmap(i, LightTexture.FULL_BRIGHT);
				data.lm[i] = class_765.field_32767;
			}
		} else {
			for (int i = 0; i < 4; i++) {
				quad.color(i, ColorHelper.multiplyRGB(quad.color(i), data.br[i]));
//				quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), data.lm[i]));
				data.lm[i] = ColorHelper.maxBrightness(quad.lightmap(i), data.lm[i]);
			}
		}
	}

	public class_11515 effectiveRenderLayer(@Nullable class_11515 quadRenderLayer) {
		return quadRenderLayer == null ? defaultLayer : quadRenderLayer;
	}

	public void emitVanillaBlockQuads(class_1087 model, @Nullable class_2680 state, Supplier<class_5819> randomSupplier, Predicate<class_2350> cullTest) {
		MutableQuadViewImpl quad = this.editorQuad;
//		final RenderMaterial defaultMaterial = state.getLightEmission() == 0 ? STANDARD_MATERIAL : NO_AO_MATERIAL;

		for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
			final class_2350 cullFace = ModelHelper.faceFromIndex(i);

			if (cullTest.test(cullFace)) {
				// Skip entire quad list if possible.
				continue;
			}

			final List<class_10889> parts = ((class_1087) this).method_68512(random);
			final int partCount = parts.size();

			for (int j = 0; j < partCount; j++) {
				parts.get(j).emitQuads(quad, cullTest);
			}
		}

	}

	// TODO move elsewhere
	record ShapePairKey(class_265 first, class_265 second) {
		public boolean equals(Object object) {
			if (object instanceof ShapePairKey shapePairKey && this.first == shapePairKey.first && this.second == shapePairKey.second) {
				return true;
			}

			return false;
		}

		public int hashCode() {
			return System.identityHashCode(this.first) * 31 + System.identityHashCode(this.second);
		}
	}

}
