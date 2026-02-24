package net.vulkanmod.interfaces.color;

import net.minecraft.class_324;
import net.vulkanmod.render.chunk.build.color.BlockColorRegistry;

public interface BlockColorsExtended {

    static BlockColorsExtended from(class_324 blockColors) {
        return (BlockColorsExtended) blockColors;
    }

    BlockColorRegistry getColorResolverMap();
}
