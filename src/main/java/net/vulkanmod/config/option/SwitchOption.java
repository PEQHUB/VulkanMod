package net.vulkanmod.config.option;

import net.minecraft.class_2561;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.gui.widget.SwitchOptionWidget;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SwitchOption extends Option<Boolean> {
    public SwitchOption(class_2561 name, Consumer<Boolean> setter, Supplier<Boolean> getter) {
        super(name, setter, getter, i -> class_2561.method_30163(String.valueOf(i)));
    }

    @Override
    public OptionWidget createWidget() {
        return new SwitchOptionWidget(this, this.name);
    }

}
