package net.vulkanmod.mixin.chunk;

import net.minecraft.class_1923;
import net.minecraft.class_2540;
import net.minecraft.class_2818;
import net.minecraft.class_2902;
import net.minecraft.class_631;
import net.minecraft.class_6603;
import net.vulkanmod.render.chunk.ChunkStatusMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(class_631.class)
public class ClientChunkCacheM {

    @Inject(method = "replaceWithPacketData", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;onChunkLoaded(Lnet/minecraft/world/level/ChunkPos;)V"))
    private void setChunkStatus(int x, int z, class_2540 friendlyByteBuf, Map<class_2902.class_2903, long[]> map,
                                Consumer<class_6603.class_6605> consumer,
                                CallbackInfoReturnable<class_2818> cir) {
        ChunkStatusMap.INSTANCE.setChunkStatus(x, z, ChunkStatusMap.DATA_READY);
    }

    @Inject(method = "drop", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;drop(ILnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void resetChunkStatus(class_1923 chunkPos, CallbackInfo ci) {
        ChunkStatusMap.INSTANCE.resetChunkStatus(chunkPos.field_9181, chunkPos.field_9180, ChunkStatusMap.DATA_READY);
    }
}
