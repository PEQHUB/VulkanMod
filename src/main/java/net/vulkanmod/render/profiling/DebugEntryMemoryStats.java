package net.vulkanmod.render.profiling;

import net.minecraft.class_11630;
import net.minecraft.class_11632;
import net.minecraft.class_1937;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.vulkanmod.render.chunk.WorldRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugEntryMemoryStats implements class_11632 {
    private static final class_2960 GROUP = class_2960.method_60656("vk_memory");

    @Override
    public void method_72751(class_11630 debugScreenDisplayer, @Nullable class_1937 level,
                        @Nullable class_2818 levelChunk, @Nullable class_2818 levelChunk2) {
        var chunkAreaManager = WorldRenderer.getInstance().getChunkAreaManager();

        if (chunkAreaManager != null) {
            debugScreenDisplayer.method_72744(
                    GROUP,
                    List.of(chunkAreaManager.getStats())
            );
        }
    }
}
