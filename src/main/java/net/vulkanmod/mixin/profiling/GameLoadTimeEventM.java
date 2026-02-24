package net.vulkanmod.mixin.profiling;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_7965;
import net.minecraft.class_7966;
import net.minecraft.class_7969;
import net.minecraft.class_8561;

@Mixin(class_8561.class)
public class GameLoadTimeEventM {

    @Shadow @Final private Map<class_7969<class_8561.class_8562>, Stopwatch> measurements;

    @Shadow @Final private static Logger LOGGER;

    @Shadow private OptionalLong bootstrapTime;

    public void send(class_7965 telemetryEventSender) {
        Map<class_7969, class_8561.class_8562> measurements = new Reference2ReferenceOpenHashMap<>();

        synchronized (this) {
            this.measurements
                    .forEach(
                            (telemetryProperty, stopwatch) -> {
                                if (!stopwatch.isRunning()) {
                                    long l = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                                    measurements.put(telemetryProperty, new class_8561.class_8562((int)l));
                                } else {
                                    LOGGER.warn(
                                            "Measurement {} was discarded since it was still ongoing when the event {} was sent.",
                                            telemetryProperty.comp_1171(),
                                            class_7966.field_44833.method_47720()
                                    );
                                }
                            }
                    );
            this.bootstrapTime.ifPresent(l -> measurements.put(class_7969.field_44835, new class_8561.class_8562((int)l)));
            this.measurements.clear();
        }

        StringBuilder stringBuilder = new StringBuilder("\n");

        for (class_7969 property : measurements.keySet()) {
            var measurement = measurements.get(property);

            stringBuilder.append("%s: %sms\n".formatted(property.comp_1171(), measurement.comp_1531()));
        }

        LOGGER.info(stringBuilder.toString());

//        telemetryEventSender.send(
//                TelemetryEventType.GAME_LOAD_TIMES,
//                builder -> {
//                    synchronized (this) {
//                        this.measurements
//                                .forEach(
//                                        (telemetryProperty, stopwatch) -> {
//                                            if (!stopwatch.isRunning()) {
//                                                long l = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//                                                builder.put(telemetryProperty, new GameLoadTimesEvent.Measurement((int)l));
//                                            } else {
//                                                LOGGER.warn(
//                                                        "Measurement {} was discarded since it was still ongoing when the event {} was sent.",
//                                                        telemetryProperty.id(),
//                                                        TelemetryEventType.GAME_LOAD_TIMES.id()
//                                                );
//                                            }
//                                        }
//                                );
//                        this.bootstrapTime.ifPresent(l -> builder.put(TelemetryProperty.LOAD_TIME_BOOTSTRAP_MS, new GameLoadTimesEvent.Measurement((int)l)));
//                        this.measurements.clear();
//                    }
//                }
//        );
    }
}
