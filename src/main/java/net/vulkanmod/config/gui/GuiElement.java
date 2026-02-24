package net.vulkanmod.config.gui;

import net.minecraft.class_156;
import net.minecraft.class_364;
import net.minecraft.class_6379;
import net.minecraft.class_6382;
import net.minecraft.class_8016;
import net.minecraft.class_8023;
import net.minecraft.class_8030;
import org.jetbrains.annotations.Nullable;

public abstract class GuiElement implements class_364, class_6379 {

    protected int width;
    protected int height;
    public int x;
    public int y;

    protected boolean hovered;
    protected long hoverStartTime;
    protected int hoverTime;
    protected long hoverStopTime;

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setPosition(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void updateState(double mX, double mY) {
        // Update hover
        if (method_25405(mX, mY)) {
            if (!this.hovered) {
                this.hoverStartTime = class_156.method_658();
            }

            this.hovered = true;
            this.hoverTime = (int) (class_156.method_658() - this.hoverStartTime);
        } else {
            if (this.hovered) {
                this.hoverStopTime = class_156.method_658();
            }
            this.hovered = false;
            this.hoverTime = 0;
        }
    }

    public float getHoverMultiplier(float time) {
        if (this.hovered) {
            return Math.min(((this.hoverTime) / time), 1.0f);
        }
        else {
            int delta = (int) (class_156.method_658() - this.hoverStopTime);
            return Math.max(1.0f - (delta / time), 0.0f);
        }
    }

    @Nullable
    @Override
    public class_8016 method_48205(class_8023 focusNavigationEvent) {
        return class_364.super.method_48205(focusNavigationEvent);
    }

    @Override
    public boolean method_25405(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y
                && mouseX <= (this.x + this.width) && mouseY <= (this.y + this.height);
    }

    @Nullable
    @Override
    public class_8016 method_48218() {
        return class_364.super.method_48218();
    }

    @Override
    public class_8030 method_48202() {
        return class_364.super.method_48202();
    }

    @Override
    public void method_25365(boolean bl) {

    }

    @Override
    public boolean method_25370() {
        return false;
    }

    @Override
    public class_6380 method_37018() {
        return class_6380.field_33784;
    }

    @Override
    public void method_37020(class_6382 narrationElementOutput) {

    }
}
