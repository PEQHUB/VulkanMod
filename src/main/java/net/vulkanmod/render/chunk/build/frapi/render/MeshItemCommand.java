package net.vulkanmod.render.chunk.build.frapi.render;

import java.util.List;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.class_10444;
import net.minecraft.class_4587;
import net.minecraft.class_777;
import net.minecraft.class_811;
import org.jspecify.annotations.Nullable;

public record MeshItemCommand(
        class_4587.class_4665 positionMatrix,
        class_811 displayContext,
        int lightCoords,
        int overlayCoords,
        int outlineColor,
        int[] tintLayers,
        List<class_777> quads,
        net.minecraft.class_1921 renderLayer,
        class_10444.class_10445 glintType,
        MeshView mesh,
        @Nullable ItemRenderTypeGetter renderTypeGetter
) {
}
