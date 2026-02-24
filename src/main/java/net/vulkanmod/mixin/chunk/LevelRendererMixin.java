package net.vulkanmod.mixin.chunk;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.class_11531;
import net.minecraft.class_11532;
import net.minecraft.class_11658;
import net.minecraft.class_11661;
import net.minecraft.class_11684;
import net.minecraft.class_12137;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_3191;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4599;
import net.minecraft.class_4604;
import net.minecraft.class_638;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_824;
import net.minecraft.class_898;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import net.minecraft.client.renderer.*;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.render.vertex.TerrainRenderType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(class_761.class)
public abstract class LevelRendererMixin {
    @Shadow @Final private Long2ObjectMap<SortedSet<class_3191>> destructionProgress;

    @Unique private WorldRenderer worldRenderer;

    @Unique double camX, camY, camZ;
    @Unique Matrix4f modelView, projection;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(class_310 minecraft, class_898 entityRenderDispatcher,
                      class_824 blockEntityRenderDispatcher, class_4599 renderBuffers,
                      class_11658 levelRenderState, class_11684 featureRenderDispatcher,
                      CallbackInfo ci) {
        this.worldRenderer = WorldRenderer.init(entityRenderDispatcher, blockEntityRenderDispatcher, renderBuffers, levelRenderState, featureRenderDispatcher);
    }

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void setLevel(class_638 clientLevel, CallbackInfo ci) {
        this.worldRenderer.setLevel(clientLevel);
    }

    @Inject(method = "allChanged", at = @At("RETURN"))
    private void onAllChanged(CallbackInfo ci) {
        this.worldRenderer.allChanged();
    }

    @Inject(method = "extractVisibleBlockEntities", at = @At("HEAD"), cancellable = true)
    private void onExtractVisibleBlockEntities(class_4184 camera, float partialTick, class_11658 levelRenderState,
                                               CallbackInfo ci) {
        this.worldRenderer.setPartialTick(partialTick);

        ci.cancel();
    }

    @Inject(method = "submitBlockEntities", at = @At(value = "RETURN"), cancellable = true)
    private void onSubmitBlockEntities(class_4587 poseStack, class_11658 levelRenderState,
                                     class_11661 submitNodeStorage, CallbackInfo ci) {
        this.worldRenderer.renderBlockEntities(poseStack, levelRenderState, submitNodeStorage, this.destructionProgress);

        ci.cancel();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void cullTerrain(class_4184 camera, class_4604 frustum, boolean spectator) {
        // TODO: port capture frustum
        this.worldRenderer.setupRenderer(camera, frustum, false, spectator);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean isSectionCompiledAndVisible(class_2338 blockPos) {
        return this.worldRenderer.isSectionCompiled(blockPos);
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void updateMatrices(class_9922 graphicsResourceAllocator, class_9779 deltaTracker,
                                boolean bl, class_4184 camera, Matrix4f modelView, Matrix4f projection, Matrix4f matrix4f,
                                GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        this.modelView = modelView;
        this.projection = projection;
    }

    @Overwrite
    private class_11532 prepareChunkRenders(Matrix4fc matrix4fc, double camX, double camY, double camZ) {
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;

        return null;
    }

    @Redirect(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V"))
    private void renderSectionLayer(class_11532 instance, class_11531 chunkSectionLayerGroup, class_12137 gpuSampler) {
        if (chunkSectionLayerGroup == class_11531.field_61022) {
            Profiler profiler = Profiler.getMainProfiler();
            profiler.push("Opaque_terrain");

            this.worldRenderer.renderSectionLayer(TerrainRenderType.SOLID, camX, camY, camZ, modelView, projection);
            this.worldRenderer.renderSectionLayer(TerrainRenderType.CUTOUT, camX, camY, camZ, modelView, projection);
        }
        else if (chunkSectionLayerGroup == class_11531.field_61023) {
            Profiler profiler = Profiler.getMainProfiler();
            profiler.pop();
            profiler.push("Translucent_terrain");

            this.worldRenderer.renderSectionLayer(TerrainRenderType.TRANSLUCENT, camX, camY, camZ, modelView, projection);

            profiler.pop();
        }

    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void onChunkReadyToRender(class_1923 chunkPos) {
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void setSectionDirty(int x, int y, int z, boolean flag) {
        this.worldRenderer.setSectionDirty(x, y, z, flag);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public String getSectionStatistics() {
        return this.worldRenderer.getChunkStatistics();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean hasRenderedAllSections() {
        return !this.worldRenderer.graphNeedsUpdate() && this.worldRenderer.getTaskDispatcher().isIdle();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int countRenderedSections() {
        return this.worldRenderer.getVisibleSectionsCount();
    }

    @Redirect(method = "addWeatherPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getDepthFar()F"))
    private float getRenderDistanceZFar(class_757 instance) {
        return instance.method_3193() * 4F;
    }

}
