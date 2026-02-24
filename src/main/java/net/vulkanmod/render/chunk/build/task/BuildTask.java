package net.vulkanmod.render.chunk.build.task;

import net.minecraft.class_11954;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2464;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3610;
import net.minecraft.class_827;
import net.minecraft.class_852;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.chunk.build.renderer.BlockRenderer;
import net.vulkanmod.render.chunk.build.renderer.FluidRenderer;
import net.vulkanmod.render.chunk.build.RenderRegion;
import net.vulkanmod.render.chunk.build.UploadBuffer;
import net.vulkanmod.render.chunk.build.thread.BuilderResources;
import net.vulkanmod.render.chunk.build.thread.ThreadBuilderPack;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.vertex.TerrainBuilder;
import net.vulkanmod.render.vertex.TerrainRenderType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BuildTask extends ChunkTask {
    @Nullable
    protected RenderRegion region;

    public BuildTask(RenderSection renderSection, RenderRegion renderRegion, boolean highPriority) {
        super(renderSection);
        this.region = renderRegion;
        this.highPriority = highPriority;
    }

    public String name() {
        return "rend_chk_rebuild";
    }

    public Result runTask(BuilderResources builderResources) {
        long startTime = System.nanoTime();

        if (this.cancelled.get()) {
            return Result.CANCELLED;
        }

        class_243 vec3 = WorldRenderer.getCameraPos();
        float x = (float) vec3.field_1352;
        float y = (float) vec3.field_1351;
        float z = (float) vec3.field_1350;
        CompileResult compileResult = this.compile(x, y, z, builderResources);

        CompiledSection compiledSection = new CompiledSection();
        compiledSection.blockEntities.addAll(compileResult.blockEntities);
        compiledSection.transparencyState = compileResult.transparencyState;
        compiledSection.isCompletelyEmpty = compileResult.renderedLayers.isEmpty();
        compileResult.compiledSection = compiledSection;

        if (this.cancelled.get()) {
            compileResult.renderedLayers.values().forEach(UploadBuffer::release);
            return Result.CANCELLED;
        }

        taskDispatcher.scheduleSectionUpdate(compileResult);

        float buildTime = (System.nanoTime() - startTime) * 0.000001f;
        if (BENCH) {
            builderResources.updateBuildStats((int) buildTime);
        }

        return Result.SUCCESSFUL;
    }

    private CompileResult compile(float camX, float camY, float camZ, BuilderResources builderResources) {
        CompileResult compileResult = new CompileResult(this.section, true);

        class_2338 startBlockPos = new class_2338(section.xOffset(), section.yOffset(), section.zOffset()).method_10062();
        class_852 visGraph = new class_852();

        if (this.region == null) {
            compileResult.visibilitySet = visGraph.method_3679();
            return compileResult;
        }

        Vector3f pos = new Vector3f();
        ThreadBuilderPack bufferBuilders = builderResources.builderPack;
        setupBufferBuilders(bufferBuilders);

        this.region.loadBlockStates();
        this.region.initTintCache(builderResources.tintCache);

        builderResources.update(this.region, this.section);

        BlockRenderer blockRenderer = builderResources.blockRenderer;

        FluidRenderer fluidRenderer = builderResources.fluidRenderer;

        class_2338.class_2339 blockPos = new class_2338.class_2339();

        for (int y = 0; y < 16; ++y) {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    blockPos.method_10103(section.xOffset() + x, section.yOffset() + y, section.zOffset() + z);

                    class_2680 blockState = this.region.method_8320(blockPos);
                    if (blockState.method_26216()) {
                        visGraph.method_3682(blockPos);
                    }

                    if (blockState.method_31709()) {
                        class_2586 blockEntity = this.region.method_8321(blockPos);
                        if (blockEntity != null) {
                            this.handleBlockEntity(compileResult, blockEntity);
                        }
                    }

                    class_3610 fluidState = blockState.method_26227();
                    if (!fluidState.method_15769()) {
                        fluidRenderer.renderLiquid(blockState, fluidState, blockPos);
                    }

                    if (blockState.method_26217() == class_2464.field_11458) {
                        pos.set(blockPos.method_10263() & 15, blockPos.method_10264() & 15, blockPos.method_10260() & 15);
                        blockRenderer.renderBlock(blockState, blockPos, pos);
                    }
                }
            }
        }

        TerrainBuilder trasnlucentTerrainBuilder = bufferBuilders.builder(TerrainRenderType.TRANSLUCENT);
        if (trasnlucentTerrainBuilder.getBufferBuilder(QuadFacing.UNDEFINED.ordinal()).getVertices() > 0) {
            trasnlucentTerrainBuilder.setupQuadSortingPoints();
            trasnlucentTerrainBuilder.setupQuadSorting(camX - (float) startBlockPos.method_10263(), camY - (float) startBlockPos.method_10264(), camZ - (float) startBlockPos.method_10260());
            compileResult.transparencyState = trasnlucentTerrainBuilder.getSortState();
        }

        for (TerrainRenderType renderType : TerrainRenderType.VALUES) {
            TerrainBuilder builder = bufferBuilders.builder(renderType);

            TerrainBuilder.DrawState drawState = builder.endDrawing();

            UploadBuffer uploadBuffer = new UploadBuffer(builder, drawState);
            compileResult.renderedLayers.put(renderType, uploadBuffer);

            builder.clear();
        }

        compileResult.visibilitySet = visGraph.method_3679();
        this.region = null;
        return compileResult;
    }

    private void setupBufferBuilders(ThreadBuilderPack builderPack) {
        for (TerrainRenderType renderType : TerrainRenderType.VALUES) {
            TerrainBuilder bufferBuilder = builderPack.builder(renderType);
            bufferBuilder.begin();
        }
    }

    private TerrainBuilder getTerrainBuilder(ThreadBuilderPack bufferBuilders, TerrainRenderType renderType) {
        renderType = compactRenderTypes(renderType);
        return bufferBuilders.builder(renderType);
    }

    private TerrainRenderType compactRenderTypes(TerrainRenderType renderType) {
        if (Initializer.CONFIG.uniqueOpaqueLayer) {
            renderType = switch (renderType) {
                case SOLID, CUTOUT -> TerrainRenderType.CUTOUT;
                case TRANSLUCENT, TRIPWIRE -> TerrainRenderType.TRANSLUCENT;
            };
        } else {
            renderType = switch (renderType) {
                case SOLID -> TerrainRenderType.SOLID;
                case CUTOUT -> TerrainRenderType.CUTOUT;
                case TRANSLUCENT, TRIPWIRE -> TerrainRenderType.TRANSLUCENT;
            };
        }

        return renderType;
    }

    private <E extends class_2586> void handleBlockEntity(CompileResult compileResult, E blockEntity) {
        class_827<E, class_11954> blockEntityRenderer = class_310.method_1551().method_31975().method_3550(blockEntity);
        if (blockEntityRenderer != null) {
            compileResult.blockEntities.add(blockEntity);
            if (blockEntityRenderer.method_3563()) {
                compileResult.globalBlockEntities.add(blockEntity);
            }
        }

    }
}
