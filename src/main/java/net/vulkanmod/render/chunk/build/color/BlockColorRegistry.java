package net.vulkanmod.render.chunk.build.color;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.class_2248;
import net.minecraft.class_322;

public class BlockColorRegistry {

	private final Reference2ReferenceOpenHashMap<class_2248, class_322> map = new Reference2ReferenceOpenHashMap<>();

	public void register(class_322 blockColor, class_2248... blocks) {
		for (class_2248 block : blocks) {
			this.map.put(block, blockColor);
		}
	}

	public class_322 getBlockColor(class_2248 block) {
		return this.map.get(block);
	}

}
