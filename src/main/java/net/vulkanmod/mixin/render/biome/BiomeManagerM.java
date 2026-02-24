package net.vulkanmod.mixin.render.biome;

import net.minecraft.class_4543;
import net.vulkanmod.interfaces.biome.BiomeManagerExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(class_4543.class)
public class BiomeManagerM implements BiomeManagerExtended {

    @Shadow @Final private long biomeZoomSeed;

    @Override
    public long getBiomeZoomSeed() {
        return this.biomeZoomSeed;
    }
}
