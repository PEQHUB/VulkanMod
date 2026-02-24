package net.vulkanmod.config.gui.widget;

import net.minecraft.class_1109;
import net.minecraft.class_1144;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import net.vulkanmod.config.gui.GuiElement;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.vulkan.util.ColorUtil;

public abstract class VAbstractWidget extends GuiElement {
    public boolean active = true;
    public boolean visible = true;
    public boolean focused;

    protected class_2561 message;

    public void setDimensions(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(double mX, double mY) {
        this.updateState(mX, mY);
        this.renderWidget(mX, mY);
    }

    public void renderWidget(double mX, double mY) {
    }

    public void onClick(double mX, double mY) {
    }

    public void onRelease(double mX, double mY) {
    }

    protected void onDrag(double mX, double mY, double f, double g) {
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    protected void renderHovering(int xPadding, int yPadding) {
        float hoverMultiplier = this.getHoverMultiplier(200);

        if (hoverMultiplier > 0.0f) {
//            int color = ColorUtil.ARGB.pack(0.5f, 0.5f, 0.5f, hoverMultiplier * 0.2f);
            int color = ColorUtil.ARGB.pack(0.3f, 0.0f, 0.0f, hoverMultiplier * 0.2f);
//            int color = ColorUtil.ARGB.multiplyAlpha(VOptionScreen.RED, hoverMultiplier);
            GuiRenderer.fill(this.x - xPadding, this.y - yPadding, this.x + this.width + xPadding, this.y + this.height + yPadding, color);

//            color = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, hoverMultiplier * 0.8f);
            color = ColorUtil.ARGB.pack(0.3f, 0.0f, 0.0f, hoverMultiplier * 0.8f);

            int x0 = this.x - xPadding;
            int x1 = this.x + this.width + xPadding;
            int y0 = this.y - yPadding;
            int y1 = this.y + height + yPadding;
            int border = 1;

            GuiRenderer.renderBorder(x0, y0, x1, y1, border, color);
        }
    }

    @Override
    public boolean method_25402(class_11909 event, boolean bl) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(event.method_74245())) {
                boolean clicked = this.clicked(event.comp_4798(), event.comp_4799());
                if (clicked) {
                    this.playDownSound(class_310.method_1551().method_1483());
                    this.onClick(event.comp_4798(), event.comp_4799());
                    return true;
                }
            }

        }
        return false;
    }

    protected boolean clicked(double mX, double mY) {
        return this.active
                && this.visible
                && mX >= (double)this.getX()
                && mY >= (double)this.getY()
                && mX < (double)(this.getX() + this.getWidth())
                && mY < (double)(this.getY() + this.getHeight());
    }

    @Override
    public boolean method_25406(class_11909 event) {
        if (this.isValidClickButton(event.method_74245())) {
            this.onRelease(event.comp_4798(), event.comp_4799());
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    @Override
    public boolean method_25403(class_11909 event, double d, double e) {
        if (this.isValidClickButton(event.method_74245())) {
            this.onDrag(event.comp_4798(), event.comp_4799(), d, e);
            return true;
        } else {
            return false;
        }
    }

    public void playDownSound(class_1144 soundManager) {
        soundManager.method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
    }

    public class_2561 getTooltip() {
        return null;
    }
}
