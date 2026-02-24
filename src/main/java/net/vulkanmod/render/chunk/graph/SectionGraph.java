package net.vulkanmod.render.chunk.graph;

import com.google.common.collect.Lists;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3695;
import net.minecraft.class_4076;
import net.minecraft.class_4184;
import net.minecraft.class_4604;
import net.vulkanmod.Initializer;
import net.vulkanmod.interfaces.FrustumMixed;
import net.vulkanmod.render.chunk.*;
import net.vulkanmod.render.chunk.build.RenderRegionBuilder;
import net.vulkanmod.render.chunk.build.task.TaskDispatcher;
import net.vulkanmod.render.chunk.frustum.VFrustum;
import net.vulkanmod.render.chunk.util.AreaSetQueue;
import net.vulkanmod.render.chunk.util.ResettableQueue;
import net.vulkanmod.render.profiling.Profiler;
import org.joml.FrustumIntersection;

import java.util.List;

public class SectionGraph {
    class_310 minecraft;
    private final class_1937 level;

    private final SectionGrid sectionGrid;
    private final ChunkAreaManager chunkAreaManager;
    private final TaskDispatcher taskDispatcher;
    private final ResettableQueue<RenderSection> sectionQueue = new ResettableQueue<>();
    private AreaSetQueue chunkAreaQueue;
    private short lastFrame = 0;

    private final ResettableQueue<RenderSection> blockEntitiesSections = new ResettableQueue<>();
    private final ResettableQueue<RenderSection> rebuildQueue = new ResettableQueue<>();

    private VFrustum frustum;

    public RenderRegionBuilder renderRegionCache;
    int nonEmptyChunks;


    public SectionGraph(class_1937 level, SectionGrid sectionGrid, TaskDispatcher taskDispatcher) {
        this.level = level;
        this.sectionGrid = sectionGrid;
        this.chunkAreaManager = sectionGrid.getChunkAreaManager();
        this.taskDispatcher = taskDispatcher;

        this.chunkAreaQueue = new AreaSetQueue(sectionGrid.getChunkAreaManager().size);
        this.minecraft = class_310.method_1551();
        this.renderRegionCache = WorldRenderer.getInstance().renderRegionCache;
    }

    public void update(class_4184 camera, class_4604 frustum, boolean spectator) {
        Profiler profiler = Profiler.getMainProfiler();
        class_3695 mcProfiler = net.minecraft.class_10209.method_64146();

        class_2338 blockpos = camera.method_19328();

        mcProfiler.method_15405("update");

        boolean flag = this.minecraft.field_1730;
        if (spectator && this.level.method_8320(blockpos).method_26216()) {
            flag = false;
        }

        profiler.push("frustum");
        this.frustum = (((FrustumMixed) (frustum)).customFrustum()).offsetToFullyIncludeCameraCube(8);
        this.sectionGrid.updateFrustumVisibility(this.frustum);
        profiler.pop();

        mcProfiler.method_15396("partial_update");

        this.initUpdate();
        this.initializeQueueForFullUpdate(camera);

        if (flag)
            this.updateRenderChunks();
        else
            this.updateRenderChunksSpectator();

        this.scheduleRebuilds();

        mcProfiler.method_15407();
    }

    private void initializeQueueForFullUpdate(class_4184 camera) {
        class_243 vec3 = camera.method_71156();
        class_2338 blockpos = camera.method_19328();
        RenderSection renderSection = this.sectionGrid.getSectionAtBlockPos(blockpos);

        if (renderSection == null) {
            boolean flag = blockpos.method_10264() > this.level.method_31607();
            int y = flag ? this.level.method_31600() - 8 : this.level.method_31607() + 8;
            int x = class_3532.method_15357(vec3.field_1352 / 16.0D) * 16;
            int z = class_3532.method_15357(vec3.field_1350 / 16.0D) * 16;

            List<RenderSection> list = Lists.newArrayList();
            int renderDistance = WorldRenderer.getInstance().getRenderDistance();

            for (int x1 = -renderDistance; x1 <= renderDistance; ++x1) {
                for (int z1 = -renderDistance; z1 <= renderDistance; ++z1) {

                    RenderSection renderSection1 = this.sectionGrid.getSectionAtBlockPos(new class_2338(x + class_4076.method_32205(x1, 8), y, z + class_4076.method_32205(z1, 8)));
                    if (renderSection1 != null) {
                        initFirstNode(renderSection1, this.lastFrame);
                        list.add(renderSection1);
                    }
                }
            }

            this.sectionQueue.ensureCapacity(list.size());
            for (RenderSection chunkInfo : list) {
                this.sectionQueue.add(chunkInfo);
            }

        } else {
            initFirstNode(renderSection, this.lastFrame);

            this.sectionQueue.add(renderSection);
        }

    }

    private static void initFirstNode(RenderSection renderSection, short frame) {
        renderSection.mainDir = 7;
        renderSection.sourceDirs = (byte) (1 << 7);
        renderSection.directions = (byte) 0xFF;
        renderSection.setLastFrame(frame);
        renderSection.visibility |= initVisibility();
        renderSection.directionChanges = 0;
        renderSection.steps = 0;
    }

    // Init special value used by first graph node
    private static long initVisibility() {
        long vis = 0;
        for (int dir = 0; dir < 6; dir++) {
            vis |= 1L << ((6 << 3) + dir);
            vis |= 1L << ((7 << 3) + dir);
        }

        return vis;
    }

    private void initUpdate() {
        this.resetUpdateQueues();

        this.lastFrame++;
        this.nonEmptyChunks = 0;
    }

    private void resetUpdateQueues() {
        this.chunkAreaQueue.clear();
        this.sectionGrid.getChunkAreaManager().resetQueues();
        this.sectionQueue.clear();
        this.blockEntitiesSections.clear();
        this.rebuildQueue.clear();
    }

    private void updateRenderChunks() {
        int maxDirectionsChanges = Initializer.CONFIG.advCulling - 1;

        while (this.sectionQueue.hasNext()) {
            RenderSection renderSection = this.sectionQueue.poll();

            if (notInFrustum(renderSection))
                continue;

            if (renderSection.directionChanges > maxDirectionsChanges)
                continue;

            if (!renderSection.isCompletelyEmpty()) {
                renderSection.getChunkArea().sectionQueue.add(renderSection);
                this.chunkAreaQueue.add(renderSection.getChunkArea());
                this.nonEmptyChunks++;
            }

            if (renderSection.containsBlockEntities()) {
                this.blockEntitiesSections.ensureCapacity(1);
                this.blockEntitiesSections.add(renderSection);
            }

            if (renderSection.isDirty()) {
                this.rebuildQueue.ensureCapacity(1);
                this.rebuildQueue.add(renderSection);
            }

            byte dirs = (byte) (renderSection.getVisibilityDirs() & renderSection.getDirections());

            visitAdjacentNodes(renderSection, dirs);
        }
    }

    private void scheduleRebuilds() {
        for (int i = 0; i < this.rebuildQueue.size(); i++) {
            RenderSection section = this.rebuildQueue.get(i);

            section.rebuildChunkAsync(this.taskDispatcher, this.renderRegionCache);
            section.setNotDirty();
        }
        this.rebuildQueue.clear();
    }

    private boolean notInFrustum(RenderSection renderSection) {
        byte frustumRes = renderSection.getChunkArea().inFrustum(renderSection.frustumIndex);
        if (frustumRes > FrustumIntersection.INTERSECT) {
            return true;
        } else if (frustumRes == FrustumIntersection.INTERSECT) {
            return !frustum.testFrustum(renderSection.xOffset, renderSection.yOffset, renderSection.zOffset,
                    renderSection.xOffset + 16, renderSection.yOffset + 16, renderSection.zOffset + 16);
        }
        return false;
    }

    private void visitAdjacentNodes(RenderSection renderSection, byte dirs) {
        dirs &= renderSection.adjDirs;

        this.sectionQueue.ensureCapacity(6);

        RenderSection relativeSection;

        relativeSection = renderSection.adjDown;
        checkToAdd(renderSection, relativeSection, (byte) GraphDirections.DOWN, (byte) GraphDirections.UP, dirs);

        relativeSection = renderSection.adjUp;
        checkToAdd(renderSection, relativeSection, (byte) GraphDirections.UP, (byte) GraphDirections.DOWN, dirs);

        relativeSection = renderSection.adjNorth;
        checkToAdd(renderSection, relativeSection, (byte) GraphDirections.NORTH, (byte) GraphDirections.SOUTH, dirs);

        relativeSection = renderSection.adjSouth;
        checkToAdd(renderSection, relativeSection, (byte) GraphDirections.SOUTH, (byte) GraphDirections.NORTH, dirs);

        relativeSection = renderSection.adjWest;
        checkToAdd(renderSection, relativeSection, (byte) GraphDirections.WEST, (byte) GraphDirections.EAST, dirs);

        relativeSection = renderSection.adjEast;
        checkToAdd(renderSection, relativeSection, (byte) GraphDirections.EAST, (byte) GraphDirections.WEST, dirs);
    }

    private void checkToAdd(RenderSection renderSection, RenderSection relativeSection, byte dir, byte opposite, byte dirs) {
        if ((dirs & (1 << dir)) != 0) {
            addNode(renderSection, relativeSection, dir, opposite);
        }
    }

    private void updateRenderChunksSpectator() {
        while (this.sectionQueue.hasNext()) {
            RenderSection renderSection = this.sectionQueue.poll();

            if (notInFrustum(renderSection))
                continue;

            if (!renderSection.isCompletelyEmpty()) {
                renderSection.getChunkArea().sectionQueue.add(renderSection);
                this.chunkAreaQueue.add(renderSection.getChunkArea());
                this.nonEmptyChunks++;
            }

            if (renderSection.isDirty()) {
                this.rebuildQueue.ensureCapacity(1);
                this.rebuildQueue.add(renderSection);
            }

            byte dirs = (byte) (renderSection.adjDirs & renderSection.getDirections());

            visitAdjacentNodes(renderSection, dirs);
        }
    }

    private void addNode(RenderSection renderSection, RenderSection relativeSection, byte direction, byte opposite) {
        if (relativeSection.getLastFrame() != this.lastFrame) {
            relativeSection.setLastFrame(this.lastFrame);

            relativeSection.mainDir = direction;
            relativeSection.sourceDirs = (byte) (1 << direction);

            final byte steps = (byte) (renderSection.steps + 1);
            relativeSection.directionChanges = (byte) (steps < 10 ? 0 : 127);
            relativeSection.steps = steps;

            relativeSection.directions = (byte) (renderSection.directions & ~(1 << opposite));
            this.sectionQueue.add(relativeSection);
        }

        relativeSection.addDir(direction);

        boolean increase = (renderSection.sourceDirs & 1 << direction) == 0 && !renderSection.isCompletelyEmpty();
        byte dc = increase ? (byte) (renderSection.directionChanges + 1) : renderSection.directionChanges;

        relativeSection.directionChanges = dc < relativeSection.directionChanges ? dc : relativeSection.directionChanges;
    }

    public AreaSetQueue getChunkAreaQueue() {
        return this.chunkAreaQueue;
    }

    public ResettableQueue<RenderSection> getSectionQueue() {
        return this.sectionQueue;
    }

    public ResettableQueue<RenderSection> getBlockEntitiesSections() {
        return this.blockEntitiesSections;
    }

    public short getLastFrame() {
        return lastFrame;
    }

    public String getStatistics() {
        int totalSections = this.sectionGrid.getSectionCount();
        int sections = this.sectionQueue.size();
        int renderDistance = WorldRenderer.getInstance().getRenderDistance();
        String tasksInfo = this.taskDispatcher == null ? "null" : this.taskDispatcher.getStats();

        return String.format("Chunks: %d(%d)/%d D: %d, %s", this.nonEmptyChunks, sections, totalSections, renderDistance, tasksInfo);
    }
}

