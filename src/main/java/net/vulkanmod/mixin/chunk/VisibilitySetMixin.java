package net.vulkanmod.mixin.chunk;

import net.minecraft.class_2350;
import net.minecraft.class_854;
import net.vulkanmod.interfaces.VisibilitySetExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(class_854.class)
public class VisibilitySetMixin implements VisibilitySetExtended {

//    private int vis2 = 0;
    private long vis = 0;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void set(class_2350 dir1, class_2350 dir2, boolean p_112989_) {
//        this.vis |= 1 << (dir1.ordinal() * 5 + dir2.ordinal()) | 1 << (dir2.ordinal() * 5 + dir1.ordinal());
        this.vis |= 1L << ((dir1.ordinal() << 3) + dir2.ordinal()) | 1L << ((dir2.ordinal() << 3) + dir1.ordinal());
//        this.vis2 |= 1L << ((dir1.ordinal() >> 1 << 3) + (dir2.ordinal() >> 1));
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setAll(boolean bl) {
        if(bl) this.vis = 0xFFFFFFFFFFFFFFFFL;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean visibilityBetween(class_2350 dir1, class_2350 dir2) {
//        return (this.vis & (1 << (dir1.ordinal() * 5 + dir2.ordinal()))) != 0;
        return (this.vis & (1L << ((dir1.ordinal() << 3) + dir2.ordinal()))) != 0;
    }

    @Override
    public long getVisibility() {
        return vis;
    }
}
