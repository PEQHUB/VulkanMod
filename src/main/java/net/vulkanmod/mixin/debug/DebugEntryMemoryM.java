package net.vulkanmod.mixin.debug;

import net.minecraft.class_11618;
import net.minecraft.class_11630;
import net.minecraft.class_1937;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.vulkanmod.vulkan.memory.MemoryManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;

@Mixin(class_11618.class)
public abstract class DebugEntryMemoryM {

    @Shadow @Final private static class_2960 GROUP;

    @Shadow
    protected static long bytesToMegabytes(long l) {
        return 0;
    }

    // TODO
//    @Shadow @Final private DebugEntryMemory.AllocationRateCalculator allocationRateCalculator;

    @Overwrite
    public void display(class_11630 debugScreenDisplayer, @Nullable class_1937 level, @Nullable class_2818 levelChunk, @Nullable class_2818 levelChunk2) {
        long l = Runtime.getRuntime().maxMemory();
        long m = Runtime.getRuntime().totalMemory();
        long n = Runtime.getRuntime().freeMemory();
        long o = m - n;
        debugScreenDisplayer.method_72744(
                GROUP,
                List.of(
                        String.format(Locale.ROOT, "Mem: %2d%% %03d/%03dMB", o * 100L / l, bytesToMegabytes(o), bytesToMegabytes(l)),
//                        String.format(Locale.ROOT, "Allocation rate: %03dMB/s", bytesToMegabytes(this.allocationRateCalculator.bytesAllocatedPerSecond(o))),
                        String.format(Locale.ROOT, "Allocated: %2d%% %03dMB", m * 100L / l, bytesToMegabytes(m)),
                        String.format("Off-heap: " + getOffHeapMemory() + "MB"),
                        "NativeMemory: %dMB".formatted(MemoryManager.getInstance().getNativeMemoryMB()),
                        "DeviceMemory: %dMB".formatted(MemoryManager.getInstance().getAllocatedDeviceMemoryMB())
                )
        );

    }

    private long getOffHeapMemory() {
        return bytesToMegabytes(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed());
    }
}
