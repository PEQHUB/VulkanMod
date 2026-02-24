package net.vulkanmod.render.chunk.build.task;

import net.minecraft.class_243;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.chunk.build.UploadBuffer;
import net.vulkanmod.render.chunk.build.thread.BuilderResources;
import net.vulkanmod.render.chunk.build.thread.ThreadBuilderPack;
import net.vulkanmod.render.vertex.QuadSorter;
import net.vulkanmod.render.vertex.TerrainBuilder;
import net.vulkanmod.render.vertex.TerrainRenderType;

public class SortTransparencyTask extends ChunkTask {

    public SortTransparencyTask(RenderSection renderSection) {
        super(renderSection);
    }

    public String name() {
        return "rend_chk_sort";
    }

    public Result runTask(BuilderResources context) {
        ThreadBuilderPack builderPack = context.builderPack;

        if (this.cancelled.get()) {
            return Result.CANCELLED;
        }

        class_243 vec3 = WorldRenderer.getCameraPos();
        float x = (float) vec3.field_1352;
        float y = (float) vec3.field_1351;
        float z = (float) vec3.field_1350;

        CompiledSection compiledSection = this.section.getCompiledSection();
        QuadSorter.SortState transparencyState = compiledSection.transparencyState;

        TerrainBuilder bufferBuilder = builderPack.builder(TerrainRenderType.TRANSLUCENT);
        bufferBuilder.begin();
        bufferBuilder.restoreSortState(transparencyState);

        bufferBuilder.setupQuadSorting(x - (float) this.section.xOffset(), y - (float) this.section.yOffset(), z - (float) this.section.zOffset());
        TerrainBuilder.DrawState drawState = bufferBuilder.endDrawing();

        CompileResult compileResult = new CompileResult(this.section, false);
        UploadBuffer uploadBuffer = new UploadBuffer(bufferBuilder, drawState);
        compileResult.renderedLayers.put(TerrainRenderType.TRANSLUCENT, uploadBuffer);

        bufferBuilder.reset();

        if (this.cancelled.get()) {
            return Result.CANCELLED;
        }

        taskDispatcher.scheduleSectionUpdate(compileResult);
        return Result.SUCCESSFUL;
    }
}
