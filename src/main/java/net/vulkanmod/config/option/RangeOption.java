package net.vulkanmod.config.option;

import net.minecraft.class_2561;
import net.minecraft.class_3532;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.gui.widget.RangeOptionWidget;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RangeOption extends Option<Integer> {
    int min;
    int max;
    int step;

    public RangeOption(class_2561 name, int min, int max, int step, Function<Integer, class_2561> translator, Consumer<Integer> setter, Supplier<Integer> getter) {
        super(name, setter, getter, translator);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public RangeOption(class_2561 name, int min, int max, int step, Consumer<Integer> setter, Supplier<Integer> getter) {
        this(name, min, max, step, (i) -> class_2561.method_43470(String.valueOf(i)), setter, getter);
    }

    public OptionWidget<?> createWidget() {
        return new RangeOptionWidget(this, this.name);
    }

    public class_2561 getName() {
        return class_2561.method_30163(this.name.getString() + ": " + this.getNewValue().toString());
    }

    public float getScaledValue() {
        float value = this.getNewValue();

        return (value - this.min) / (this.max - this.min);
    }

    public void setValue(float f) {
        double n = class_3532.method_16439(f, min, max);

        n = this.step * Math.round(n / this.step);

        this.setNewValue((int) n);
    }
}
