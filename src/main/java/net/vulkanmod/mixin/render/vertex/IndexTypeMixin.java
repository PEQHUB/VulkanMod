package net.vulkanmod.mixin.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VertexFormat.class_5595.class)
public class IndexTypeMixin {

    /**
     * @author
     */
    @Overwrite
    public static VertexFormat.class_5595 least(int number) {
        return VertexFormat.class_5595.field_27372;
    }
}