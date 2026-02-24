package net.vulkanmod.render.chunk.build.light.flat;

import net.vulkanmod.render.chunk.util.SimpleDirection;
import net.vulkanmod.render.model.quad.ModelQuadView;
import net.vulkanmod.render.chunk.build.light.data.LightDataAccess;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_765;
import net.vulkanmod.render.chunk.build.light.LightPipeline;
import net.vulkanmod.render.chunk.build.light.data.QuadLightData;
import net.vulkanmod.render.model.quad.ModelQuadFlags;

import java.util.Arrays;

import static net.vulkanmod.render.chunk.build.light.data.LightDataAccess.*;

/**
 * A light pipeline which implements "classic-style" lighting through simply using the light value of the adjacent
 * block to a face.
 */
public class FlatLightPipeline implements LightPipeline {
    /**
     * The cache which light data will be accessed from.
     */
    private final LightDataAccess lightCache;

    public FlatLightPipeline(LightDataAccess lightCache) {
        this.lightCache = lightCache;
    }

    @Override
    public void calculate(ModelQuadView quad, class_2338 pos, QuadLightData out, class_2350 cullFace, class_2350 lightFace, boolean shade) {
        int lightmap;

        // To match vanilla behavior, use the cull face if it exists/is available
        if (cullFace != null) {
            lightmap = getLightmap(pos, cullFace);
        } else {
            int flags = quad.getFlags();
            // If the face is aligned, use the light data above it
            // To match vanilla behavior, also treat the face as aligned if it is parallel and the block state is a full cube
            if ((flags & ModelQuadFlags.IS_ALIGNED) != 0 || ((flags & ModelQuadFlags.IS_PARALLEL) != 0 && unpackFC(this.lightCache.get(pos)))) {
                lightmap = getLightmap(pos, lightFace);
            } else {
                lightmap = getEmissiveLightmap(this.lightCache.get(pos));
            }
        }

        Arrays.fill(out.lm, lightmap);
        Arrays.fill(out.br, this.lightCache.getRegion().method_24852(lightFace, shade));
    }

    private int getLightmap(class_2338 pos, class_2350 face) {
        int word = this.lightCache.get(pos);

        // Check emissivity of the origin state
        if (unpackEM(word)) {
            return class_765.field_32767;
        }

        int adjWord = this.lightCache.get(pos, SimpleDirection.of(face));
        return LightDataAccess.getLightmap(adjWord);
    }
}
