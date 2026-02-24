package net.vulkanmod.config.gui.widget;

import net.minecraft.class_1109;
import net.minecraft.class_1144;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3417;
import net.minecraft.class_6379;
import net.minecraft.class_6382;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.vulkan.util.ColorUtil;

public abstract class OptionWidget<O extends Option<?>> extends VAbstractWidget implements class_6379 {
    public int controlX;
    public int controlWidth;
    private final class_2561 name;
    protected class_2561 displayedValue;

    protected boolean controlHovered;

    final O option;

    public OptionWidget(O option, class_2561 name) {
        this.option = option;
        this.name = name;
        this.displayedValue = class_2561.method_43470("N/A");
    }

    @Override
    public void setDimensions(int x, int y, int width, int height) {
        super.setDimensions(x, y, width, height);

        this.controlWidth = Math.min((int) (width * 0.5f) - 8, 120);
        this.controlX = this.x + this.width - this.controlWidth - 8;
    }

    public void render(double mouseX, double mouseY) {
        if (!this.visible) {
            return;
        }

        this.updateDisplayedValue();

        this.controlHovered = mouseX >= this.controlX && mouseY >= this.y && mouseX < this.controlX + this.controlWidth && mouseY < this.y + this.height;
        this.renderWidget(mouseX, mouseY);
    }

    public void updateState() {

    }

    public void renderWidget(double mouseX, double mouseY) {
        class_310 minecraftClient = class_310.method_1551();

        int i = this.getYImage(this.isHovered());

        int xPadding = 0;
        int yPadding = 0;

        int color = ColorUtil.ARGB.pack(0.0f, 0.0f, 0.0f, 0.45f);
        GuiRenderer.fill(this.x - xPadding, this.y - yPadding, this.x + this.width + xPadding, this.y + this.height + yPadding, color);

        this.renderHovering(0, 0);

        color = this.active ? 0xFFFFFFFF : 0xFFA0A0A0;

        class_327 textRenderer = minecraftClient.field_1772;
        GuiRenderer.drawString(textRenderer, this.getName().method_30937(), this.x + 8, this.y + (this.height - 8) / 2, color);

        this.renderControls(mouseX, mouseY);
    }

    protected int getYImage(boolean hovered) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (hovered) {
            i = 2;
        }
        return i;
    }

    public boolean isHovered() {
        return this.hovered || this.focused;
    }

    protected abstract void renderControls(double mouseX, double mouseY);

    public abstract void onClick(double mouseX, double mouseY);

    public abstract void onRelease(double mouseX, double mouseY);

    protected abstract void onDrag(double mouseX, double mouseY, double deltaX, double deltaY);

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    @Override
    public boolean method_25403(class_11909 event, double deltaX, double deltaY) {
        if (this.isValidClickButton(event.method_74245())) {
            this.onDrag(event.comp_4798(), event.comp_4799(), deltaX, deltaY);
            return true;
        }
        return false;
    }

    @Override
    public boolean method_25402(class_11909 event, boolean bl) {
        if (!this.active || !this.visible) {
            return false;
        }

        if (this.isValidClickButton(event.method_74245()) && this.clicked(event.comp_4798(), event.comp_4799())) {
            this.playDownSound(class_310.method_1551().method_1483());
            this.onClick(event.comp_4798(), event.comp_4799());
            return true;
        }
        return false;
    }

    @Override
    public boolean method_25406(class_11909 event) {
        if (this.isValidClickButton(event.method_74245())) {
            this.onRelease(event.comp_4798(), event.comp_4799());
            return true;
        }
        return false;
    }

    @Override
    public boolean method_25405(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height);
    }

    @Override
    public void method_25365(boolean bl) {
        this.focused = bl;
    }

    @Override
    public boolean method_25370() {
        return this.focused;
    }

    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double)this.controlX && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height);
    }

    public class_2561 getName() {
        return this.name;
    }

    public class_2561 getDisplayedValue() {
        return this.displayedValue;
    }

    protected void updateDisplayedValue() {
        this.displayedValue = this.option.getDisplayedValue();
    }

    public class_2561 getTooltip() {
        return this.option.getTooltip();
    }

    @Override
    public class_6380 method_37018() {
        if (this.focused) {
            return class_6380.field_33786;
        }
        if (this.hovered) {
            return class_6380.field_33785;
        }
        return class_6380.field_33784;
    }

    @Override
    public final void method_37020(class_6382 narrationElementOutput) {
    }

    public void playDownSound(class_1144 soundManager) {
        soundManager.method_4873(class_1109.method_47978(class_3417.field_15015, 1.0f));
    }

}
