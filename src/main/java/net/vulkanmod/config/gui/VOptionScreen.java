package net.vulkanmod.config.gui;

import com.google.common.collect.Lists;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_5481;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.config.gui.widget.VButtonWidget;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class VOptionScreen extends class_437 {
    public final static int RED = ColorUtil.ARGB.pack(0.3f, 0.0f, 0.0f, 0.8f);
    final class_2960 ICON = class_2960.method_60655("vulkanmod", "vlogo_transparent.png");

    private final class_437 parent;

    private final List<OptionPage> optionPages;

    private int currentListIdx = 0;

    private int tooltipX;
    private int tooltipY;
    private int tooltipWidth;

    private VButtonWidget supportButton;

    private VButtonWidget doneButton;
    private VButtonWidget applyButton;

    private final List<VButtonWidget> pageButtons = Lists.newArrayList();
    private final List<VButtonWidget> buttons = Lists.newArrayList();

    public VOptionScreen(class_2561 title, class_437 parent) {
        super(title);
        this.parent = parent;

        this.optionPages = new ArrayList<>();
    }

    private void addPages() {
        this.optionPages.clear();

        OptionPage page = new OptionPage(
                class_2561.method_43471("vulkanmod.options.pages.video").getString(),
                Options.getVideoOpts()
        );
        this.optionPages.add(page);

        page = new OptionPage(
                class_2561.method_43471("vulkanmod.options.pages.graphics").getString(),
                Options.getGraphicsOpts()
        );
        this.optionPages.add(page);

        page = new OptionPage(
                class_2561.method_43471("vulkanmod.options.pages.optimizations").getString(),
                Options.getOptimizationOpts()
        );
        this.optionPages.add(page);

        page = new OptionPage(
                class_2561.method_43471("vulkanmod.options.pages.other").getString(),
                Options.getOtherOpts()
        );
        this.optionPages.add(page);
    }

    @Override
    protected void method_25426() {
        this.addPages();

        int top = 40;
        int bottom = 60;
        int itemHeight = 20;

        int leftMargin = 100;
//        int listWidth = (int) (this.width * 0.65f);
        int listWidth = Math.min((int) (this.field_22789 * 0.65f), 420);
        int listHeight = this.field_22790 - top - bottom;

        this.buildLists(leftMargin, top, listWidth, listHeight, itemHeight);

        int x = leftMargin + listWidth + 10;
//        int width = Math.min(this.width - this.tooltipX - 10, 200);
        int width = this.field_22789 - x - 10;
        int y = 50;

        if (width < 200) {
            x = 100;
            width = listWidth;
            y = this.field_22790 - bottom + 10;
        }

        this.tooltipX = x;
        this.tooltipY = y;
        this.tooltipWidth = width;

        buildPage();

        this.applyButton.active = false;
    }

    private void buildLists(int left, int top, int listWidth, int listHeight, int itemHeight) {
        for (OptionPage page : this.optionPages) {
            page.createList(left, top, listWidth, listHeight, itemHeight);
            page.updateOptionStates();
        }
    }

    private void addPageButtons(int x0, int y0, int width, int height, boolean verticalLayout) {
        int x = x0;
        int y = y0;
        for (int i = 0; i < this.optionPages.size(); ++i) {
            var page = this.optionPages.get(i);
            final int finalIdx = i;
            VButtonWidget widget = new VButtonWidget(x, y, width, height, class_2561.method_30163(page.name), button -> this.setOptionList(finalIdx));
            this.buttons.add(widget);
            this.pageButtons.add(widget);
            this.method_25429(widget);

            if (verticalLayout)
                y += height + 1;
            else
                x += width + 1;
        }

        this.pageButtons.get(this.currentListIdx).setSelected(true);
    }

    private void buildPage() {
        this.buttons.clear();
        this.pageButtons.clear();
        this.method_37067();

//        this.addPageButtons(20, 6, 60, 20, false);
        this.addPageButtons(10, 40, 80, 22, true);

        VOptionList currentList = this.optionPages.get(this.currentListIdx).getOptionList();
        this.method_25429(currentList);

        this.addButtons();
    }

    private void addButtons() {
        int rightMargin = 20;
        int buttonHeight = 20;
        int padding = 10;
        int buttonMargin = 5;
        int buttonWidth = field_22787.field_1772.method_27525(class_5244.field_24334) + 2 * padding;
        int x0 = (this.field_22789 - buttonWidth - rightMargin);
        int y0 = this.field_22790 - buttonHeight - 7;

        this.doneButton = new VButtonWidget(
                x0, y0,
                buttonWidth, buttonHeight,
                class_5244.field_24334,
                button -> this.field_22787.method_1507(this.parent)
        );

        buttonWidth = field_22787.field_1772.method_27525(class_2561.method_43471("vulkanmod.options.buttons.apply")) + 2 * padding;
        x0 -= (buttonWidth + buttonMargin);
        this.applyButton = new VButtonWidget(
                x0, y0,
                buttonWidth, buttonHeight,
                class_2561.method_43471("vulkanmod.options.buttons.apply"),
                button -> this.applyOptions()
        );

        buttonWidth = field_22787.field_1772.method_27525(class_2561.method_43471("vulkanmod.options.buttons.kofi")) + 10;
        x0 = (this.field_22789 - buttonWidth - rightMargin);
        this.supportButton = new VButtonWidget(
                x0, 6,
                buttonWidth, buttonHeight,
                class_2561.method_43471("vulkanmod.options.buttons.kofi"),
                button -> class_156.method_668().method_670("https://ko-fi.com/xcollateral")
        );

        this.buttons.add(this.applyButton);
        this.buttons.add(this.doneButton);
        this.buttons.add(this.supportButton);

        this.method_25429(this.applyButton);
        this.method_25429(this.doneButton);
        this.method_25429(this.supportButton);
    }

    @Override
    public boolean method_25402(class_11909 event, boolean bl) {
        for (class_364 element : this.method_25396()) {
            if (element.method_25402(event, bl)) {
                this.method_25395(element);
                if (event.method_74245() == 0) {
                    this.method_25398(true);
                }

                this.updateState();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean method_25406(class_11909 event) {
        this.method_25398(false);
        this.updateState();
        return this.method_19355(event.comp_4798(), event.comp_4799())
                .filter(guiEventListener -> guiEventListener.method_25406(event))
                .isPresent();
    }

    @Override
    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    @Override
    public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float delta) {
        GuiRenderer.guiGraphics = guiGraphics;
        VRenderSystem.enableBlend();

        int size = field_22787.field_1772.field_2000 * 4;

        guiGraphics.method_25290(class_10799.field_56883, ICON, 30, 4, 0f, 0f, size, size, size, size);

        VOptionList currentList = this.optionPages.get(this.currentListIdx).getOptionList();
        currentList.updateState(mouseX, mouseY);
        currentList.renderWidget(mouseX, mouseY);
        renderButtons(mouseX, mouseY);

        List<class_5481> list = getHoveredButtonTooltip(currentList, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(list, this.tooltipX, this.tooltipY);
        }
    }

    public void renderButtons(int mouseX, int mouseY) {
        for (VButtonWidget button : buttons) {
            button.render(mouseX, mouseY);
        }
    }

    private void renderTooltip(List<class_5481> list, int x, int y) {
        int padding = 3;
        int width = GuiRenderer.getMaxTextWidth(this.field_22793, list);
        int height = list.size() * 10;
        float intensity = 0.05f;
        int color = ColorUtil.ARGB.pack(intensity, intensity, intensity, 0.6f);
        GuiRenderer.fill(x - padding, y - padding, x + width + padding, y + height + padding, color);

        color = RED;
        GuiRenderer.renderBorder(x - padding, y - padding, x + width + padding, y + height + padding, 1, color);

        int yOffset = 0;
        for (var text : list) {
            GuiRenderer.drawString(this.field_22793, text, x, y + yOffset, 0xffffffff);
            yOffset += 10;
        }
    }

    private List<class_5481> getHoveredButtonTooltip(VOptionList buttonList, int mouseX, int mouseY) {
        VAbstractWidget widget = buttonList.getHoveredWidget(mouseX, mouseY);
        if (widget != null) {
            var tooltip = widget.getTooltip();
            if (tooltip == null)
                return null;

            return this.field_22793.method_1728(tooltip, this.tooltipWidth);
        }
        return null;
    }

    private void updateState() {
        boolean modified = false;
        for (var page : this.optionPages) {
            modified |= page.optionChanged();
        }

        if (modified) {
            for (var page : this.optionPages) {
                page.optionChanged();
            }
        }

        this.applyButton.active = modified;
    }

    private void setOptionList(int i) {
        this.currentListIdx = i;

        this.buildPage();

        this.pageButtons.get(i).setSelected(true);
    }

    private void applyOptions() {
        List<OptionPage> pages = List.copyOf(this.optionPages);
        for (var page : pages) {
            page.applyOptionChanges();
            page.updateOptionStates();
        }

        Initializer.CONFIG.write();
    }
}
