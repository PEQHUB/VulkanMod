package net.vulkanmod.render.chunk.build.frapi.render;

import java.util.Arrays;
import java.util.List;

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.class_10444;
import net.minecraft.class_11515;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4722;
import net.minecraft.class_765;
import net.minecraft.class_777;
import net.minecraft.class_7837;
import net.minecraft.class_811;
import net.minecraft.class_918;
import net.vulkanmod.mixin.render.frapi.ItemRendererAccessor;
import net.vulkanmod.render.chunk.build.frapi.helper.ColorHelper;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableQuadViewImpl;
import org.jetbrains.annotations.Nullable;


/**
 * Used during item buffering to support geometry added through {@link FabricLayerRenderState#emitter()}.
 */
public class ItemRenderContext extends AbstractRenderContext {
	private static final int GLINT_COUNT = class_10444.class_10445.values().length;

	private class_811 itemDisplayContext;
	private class_4587 matrixStack;
	private class_4597 vertexConsumerProvider;
	private int lightmap;
	private int[] tints;

	private class_1921 defaultLayer;
	private class_10444.class_10445 defaultGlint;
    private boolean ignoreQuadGlint;

	private class_4587.class_4665 specialGlintEntry;
	private final class_4588[] vertexConsumerCache = new class_4588[3 * GLINT_COUNT];

	public void renderModel(class_811 itemDisplayContext, class_4587 matrixStack, class_4597 bufferSource, int lightmap, int overlay, int[] tints, List<class_777> modelQuads, MeshView mesh, class_1921 renderType, class_10444.class_10445 foilType, boolean ignoreQuadGlint) {
		this.itemDisplayContext = itemDisplayContext;
		this.matrixStack = matrixStack;
		this.vertexConsumerProvider = bufferSource;
		this.lightmap = lightmap;
		this.overlay = overlay;
		this.tints = tints;

		defaultLayer = renderType;
		defaultGlint = foilType;
        this.ignoreQuadGlint = ignoreQuadGlint;

		bufferQuads(modelQuads, mesh);

		this.matrixStack = null;
		this.vertexConsumerProvider = null;
		this.tints = null;

		specialGlintEntry = null;
		Arrays.fill(vertexConsumerCache, null);
	}

    private void bufferQuads(List<class_777> vanillaQuads, MeshView mesh) {
        QuadEmitter emitter = getEmitter();

        final int vanillaQuadCount = vanillaQuads.size();

        for (int i = 0; i < vanillaQuadCount; i++) {
            final class_777 q = vanillaQuads.get(i);
            emitter.fromBakedQuad(q);
            emitter.emit();
        }

        mesh.outputTo(emitter);
    }

	@Override
	protected void bufferQuad(MutableQuadViewImpl quad) {
        final class_4588 vertexConsumer = getVertexConsumer(quad.renderLayer(), quad.glint());

        tintQuad(quad);
        shadeQuad(quad, quad.emissive());
        bufferQuad(quad, vertexConsumer);
	}

	private void tintQuad(MutableQuadViewImpl quad) {
		int tintIndex = quad.tintIndex();

		if (tintIndex != -1 && tintIndex < tints.length) {
			final int tint = tints[tintIndex];

			for (int i = 0; i < 4; i++) {
				quad.color(i, ColorHelper.multiplyColor(tint, quad.color(i)));
			}
		}
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
		if (emissive) {
			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, class_765.field_32767);
			}
		} else {
			final int lightmap = this.lightmap;

			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), lightmap));
			}
		}
	}

    private class_4588 getVertexConsumer(@Nullable class_11515 quadRenderLayer, @Nullable class_10444.class_10445 quadGlint) {
        class_1921 layer;
        class_10444.class_10445 glint;

        if (quadRenderLayer == null) {
            layer = defaultLayer;
        } else {
            layer = RenderLayerHelper.getEntityBlockLayer(quadRenderLayer);
        }

        if (ignoreQuadGlint || quadGlint == null) {
            glint = defaultGlint;
        } else {
            glint = quadGlint;
        }

        int cacheIndex;

        if (layer == class_4722.method_29382()) {
            cacheIndex = 0;
        } else if (layer == class_4722.method_24074()) {
            cacheIndex = GLINT_COUNT;
        } else {
            cacheIndex = 2 * GLINT_COUNT;
        }

        cacheIndex += glint.ordinal();
        class_4588 vertexConsumer = vertexConsumerCache[cacheIndex];

        if (vertexConsumer == null) {
            vertexConsumer = createVertexConsumer(layer, glint);
            vertexConsumerCache[cacheIndex] = vertexConsumer;
        }

        return vertexConsumer;
    }

	private class_4588 createVertexConsumer(class_1921 layer, class_10444.class_10445 glint) {
		if (glint == class_10444.class_10445.field_55343) {
			if (specialGlintEntry == null) {
				specialGlintEntry = matrixStack.method_23760().method_56822();

				if (itemDisplayContext == class_811.field_4317) {
					class_7837.method_46414(specialGlintEntry.method_23761(), 0.5F);
				} else if (itemDisplayContext.method_29998()) {
					class_7837.method_46414(specialGlintEntry.method_23761(), 0.75F);
				}
			}

			return ItemRendererAccessor.getSpecialFoilBuffer(vertexConsumerProvider, layer, specialGlintEntry);
		}

		return class_918.method_23181(vertexConsumerProvider, layer, true, glint != class_10444.class_10445.field_55341);
	}

}
