package net.vulkanmod.render.chunk.util;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import net.minecraft.class_2350;

public class Util {

    public static final class_2350[] DIRECTIONS = class_2350.values();
    public static final class_2350[] XZ_DIRECTIONS = getXzDirections();

    public static byte getOppositeDirIdx(byte idx) {
        return (byte) ((idx & 0b1) != 0 ? idx - 1 : idx + 1);
    }

    private static class_2350[] getXzDirections() {
        class_2350[] directions = new class_2350[4];

        int i = 0;
        for (class_2350 direction : class_2350.values()) {
            if (direction.method_10166() == class_2350.class_2351.field_11048 || direction.method_10166() == class_2350.class_2351.field_11051) {
                directions[i] = direction;
                ++i;
            }
        }
        return directions;
    }

    public static long posLongHash(int x, int y, int z) {
        return (long) x & 0x00000000FFFFL | ((long) z << 16) & 0x0000FFFF0000L | ((long) y << 32) & 0xFFFF00000000L;
    }

    public static int flooredLog(int v) {
        assert v > 0;
        int log = 30;
        int t = 0x40000000;

        while ((v & t) == 0) {
            t >>= 1;
            log--;
        }

        return log;
    }

    public static long align(long l, int alignment) {
        if (alignment == 0)
            return l;

        long r = l % alignment;
        return r != 0 ? l + alignment - r : l;
    }
}
