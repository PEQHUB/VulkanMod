package net.vulkanmod.mixin.chunk;

import net.minecraft.class_1923;
import net.minecraft.class_2666;
import net.minecraft.class_634;
import net.minecraft.class_6606;
import net.vulkanmod.render.chunk.ChunkStatusMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
public class ClientPacketListenerM {

    @Inject(method = "applyLightData", at = @At("RETURN"))
    private void setChunkStatus(int x, int z, class_6606 clientboundLightUpdatePacketData, boolean bl, CallbackInfo ci) {
        ChunkStatusMap.INSTANCE.setChunkStatus(x, z, ChunkStatusMap.LIGHT_READY);
    }

    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void resetChunkStatus(class_2666 clientboundForgetLevelChunkPacket, CallbackInfo ci) {
        class_1923 chunkPos = clientboundForgetLevelChunkPacket.comp_1726();
        ChunkStatusMap.INSTANCE.resetChunkStatus(chunkPos.field_9181, chunkPos.field_9180, ChunkStatusMap.LIGHT_READY);
    }
}
