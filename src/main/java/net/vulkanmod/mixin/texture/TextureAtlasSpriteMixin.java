package net.vulkanmod.mixin.texture;

import com.mojang.blaze3d.buffers.Std140Builder;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;
import net.minecraft.class_1058;
import net.minecraft.class_7764;

@Mixin(class_1058.class)
public class TextureAtlasSpriteMixin {
    @Shadow @Final private class_7764 contents;
    @Shadow @Final private int padding;
    @Shadow @Final private int x;
    @Shadow @Final private int y;

    @Overwrite
    public void uploadSpriteUbo(ByteBuffer byteBuffer, int i, int maxMipLevel, int width, int height, int uboSize) {
        for (int n = 0; n <= maxMipLevel; n++) {
            Std140Builder.intoBuffer(MemoryUtil.memSlice(byteBuffer, i + n * uboSize, uboSize))
                         .putMat4f(new Matrix4f().ortho2D(0.0F, width >> n, height >> n, 0))
                         .putMat4f(
                                 new Matrix4f()
                                         .translate(this.x >> n, this.y >> n, 0.0F)
                                         .scale(this.contents.method_45807() + this.padding * 2 >> n, this.contents.method_45815() + this.padding * 2 >> n, 1.0F)
                         )
                         .putFloat((float)this.padding / this.contents.method_45807())
                         .putFloat((float)this.padding / this.contents.method_45815())
                         .putInt(n);
        }
    }
}
