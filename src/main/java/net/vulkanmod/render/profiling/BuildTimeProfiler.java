package net.vulkanmod.render.profiling;

import net.minecraft.class_310;

public abstract class BuildTimeProfiler {

    private static boolean bench = false;
    private static long startTime;
    private static float deltaTime;

    public static void runBench(boolean building) {
        if(bench) {
            if (startTime == 0) {
                startTime = System.nanoTime();
            }

            if(!building) {
                deltaTime = (System.nanoTime() - startTime) * 0.000001f;
                bench = false;
                startTime = 0;
            }
        }
    }

    public static void startBench() {
        bench = true;
        class_310.method_1551().field_1769.method_3279();
    }

    public static float getDeltaTime() {
        return deltaTime;
    }
}
