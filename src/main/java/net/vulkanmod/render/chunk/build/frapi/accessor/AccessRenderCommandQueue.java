package net.vulkanmod.render.chunk.build.frapi.accessor;

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.class_10444;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_777;
import net.minecraft.class_811;
import org.jspecify.annotations.Nullable;


import java.util.List;

public interface AccessRenderCommandQueue {
	void submitItem(
			class_4587 matrices,
			class_811 displayContext,
			int light,
			int overlay,
			int outlineColors,
			int[] tintLayers,
			List<class_777> quads,
			class_1921 renderLayer,
			class_10444.class_10445 glintType,
			MeshView mesh,
			@Nullable ItemRenderTypeGetter renderTypeGetter
	);
}
