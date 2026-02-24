package net.vulkanmod.mixin.render.block;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;
import net.vulkanmod.render.chunk.build.frapi.helper.NormalHelper;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.model.quad.ModelQuadView;
import net.vulkanmod.render.model.quad.ModelQuadFlags;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public abstract class BakedQuadM implements ModelQuadView {

    @Shadow @Final private int tintIndex;
    @Shadow @Final private Direction direction;

    @Shadow public abstract Vector3fc position(int index);
    @Shadow public abstract long packedUV(int index);

    @Unique private int flags = -1;
    @Unique private int normal;
    @Unique private QuadFacing facing;

    @Unique
    private void ensureComputed() {
        if (flags == -1) {
            flags = ModelQuadFlags.getQuadFlags(this, direction);
            normal = NormalHelper.computePackedNormal(this);
            facing = QuadFacing.fromNormal(normal);
        }
    }

    @Override
    public int getFlags() {
        ensureComputed();
        return flags;
    }

    @Override
    public float getX(int idx) {
        return position(idx).x();
    }

    @Override
    public float getY(int idx) {
        return position(idx).y();
    }

    @Override
    public float getZ(int idx) {
        return position(idx).z();
    }

    @Override
    public int getColor(int idx) {
        // BakedQuad records don't store per-vertex color; return white
        return -1;
    }

    @Override
    public float getU(int idx) {
        return UVPair.unpackU(packedUV(idx));
    }

    @Override
    public float getV(int idx) {
        return UVPair.unpackV(packedUV(idx));
    }

    @Override
    public int getColorIndex() {
        return this.tintIndex;
    }

    @Override
    public Direction lightFace() {
        return this.direction;
    }

    @Override
    public Direction getFacingDirection() {
        return this.direction;
    }

    @Override
    public QuadFacing getQuadFacing() {
        ensureComputed();
        return this.facing;
    }

    @Override
    public int getNormal() {
        ensureComputed();
        return this.normal;
    }

    @Override
    public boolean isTinted() {
        return this.tintIndex != -1;
    }
}
