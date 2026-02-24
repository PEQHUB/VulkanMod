package net.vulkanmod.render.chunk.build.task;

import com.google.common.collect.Lists;
import net.minecraft.class_2586;
import net.vulkanmod.render.vertex.QuadSorter;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class CompiledSection {
    public static final CompiledSection UNCOMPILED = new CompiledSection();

    boolean isCompletelyEmpty = false;
    final List<class_2586> blockEntities = Lists.newArrayList();
    @Nullable QuadSorter.SortState transparencyState;

    public boolean hasTransparencyState() {
        return this.transparencyState != null;
    }

    public List<class_2586> getBlockEntities() {
        return this.blockEntities;
    }
}


