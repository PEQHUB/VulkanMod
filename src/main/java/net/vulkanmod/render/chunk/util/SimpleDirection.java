package net.vulkanmod.render.chunk.util;

import net.minecraft.class_2350;
import net.minecraft.class_2382;

public enum SimpleDirection {
    DOWN(0, 1, -1, new class_2382(0, -1, 0)),
    UP(1, 0, -1, new class_2382(0, 1, 0)),
    NORTH(2, 3, 2, new class_2382(0, 0, -1)),
    SOUTH(3, 2, 0, new class_2382(0, 0, 1)),
    WEST(4, 5, 1, new class_2382(-1, 0, 0)),
    EAST(5, 4, 3, new class_2382(1, 0, 0));

    private static final SimpleDirection[] VALUES = SimpleDirection.values();

    public static SimpleDirection of(class_2350 direction) {
        return VALUES[direction.method_10146()];
    }

    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;

    public final byte nx, ny, nz;

    SimpleDirection(int j, int k, int l, class_2382 normal) {
        this.data3d = j;
        this.oppositeIndex = k;
        this.data2d = l;

        this.nx = (byte) normal.method_10263();
        this.ny = (byte) normal.method_10264();
        this.nz = (byte) normal.method_10260();
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public byte getStepX() {
        return this.nx;
    }

    public byte getStepY() {
        return this.ny;
    }

    public byte getStepZ() {
        return this.nz;
    }
}
