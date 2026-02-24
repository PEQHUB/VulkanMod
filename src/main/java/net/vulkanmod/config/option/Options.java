package net.vulkanmod.config.option;

import net.minecraft.class_1041;
import net.minecraft.class_12393;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4061;
import net.minecraft.class_4063;
import net.minecraft.class_4066;
import net.minecraft.class_5365;
import net.minecraft.class_6597;
import net.minecraft.class_9927;
import net.minecraft.client.*;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.chunk.build.light.LightMode;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.device.DeviceManager;

import java.util.stream.IntStream;

public abstract class Options {
    public static boolean fullscreenDirty = false;
    static Config config = Initializer.CONFIG;
    static class_310 minecraft = class_310.method_1551();
    static class_1041 window = minecraft.method_22683();
    static net.minecraft.class_315 minecraftOptions = minecraft.field_1690;

    public static OptionBlock[] getVideoOpts() {
        var videoMode = config.videoMode;
        var videoModeSet = VideoModeManager.getFromVideoMode(videoMode);

        if (videoModeSet == null) {
            videoModeSet = VideoModeSet.getDummy();
            videoMode = videoModeSet.getVideoMode(-1);
        }

        VideoModeManager.selectedVideoMode = videoMode;
        var refreshRates = videoModeSet.getRefreshRates();

        CyclingOption<Integer> RefreshRate = (CyclingOption<Integer>) new CyclingOption<>(
                class_2561.method_43471("vulkanmod.options.refreshRate"),
                refreshRates.toArray(new Integer[0]),
                (value) -> {
                    VideoModeManager.selectedVideoMode.refreshRate = value;
                    VideoModeManager.applySelectedVideoMode();

                    if (minecraftOptions.method_42447().method_41753())
                        fullscreenDirty = true;
                },
                () -> VideoModeManager.selectedVideoMode.refreshRate)
                .setTranslator(refreshRate -> class_2561.method_30163(refreshRate.toString()));

        Option<VideoModeSet> resolutionOption = new CyclingOption<>(
                class_2561.method_43471("options.fullscreen.resolution"),
                VideoModeManager.getVideoResolutions(),
                (value) -> {
                    VideoModeManager.selectedVideoMode = value.getVideoMode(RefreshRate.getNewValue());
                    VideoModeManager.applySelectedVideoMode();

                    if (minecraftOptions.method_42447().method_41753())
                        fullscreenDirty = true;
                },
                () -> {
                    var selectedVideoMode = VideoModeManager.selectedVideoMode;
                    var selectedVideoModeSet = VideoModeManager.getFromVideoMode(selectedVideoMode);

                    return selectedVideoModeSet != null ? selectedVideoModeSet : VideoModeSet.getDummy();
                })
                .setTranslator(resolution -> class_2561.method_30163(resolution.toString()));

        resolutionOption.setOnChange(() -> {
            var newVideoMode = resolutionOption.getNewValue();
            var newRefreshRates = newVideoMode.getRefreshRates().toArray(new Integer[0]);

            RefreshRate.setValues(newRefreshRates);
            RefreshRate.setNewValue(newRefreshRates[newRefreshRates.length - 1]);
        });

        return new OptionBlock[]{
                new OptionBlock("", new Option<?>[]{
                        resolutionOption,
                        RefreshRate,
                        new CyclingOption<>(class_2561.method_43471("vulkanmod.options.windowMode"),
                                WindowMode.values(),
                                value -> {
                                    boolean exclusiveFullscreen = value == WindowMode.EXCLUSIVE_FULLSCREEN;
                                    minecraftOptions.method_42447()
                                                    .method_41748(exclusiveFullscreen);

                                    config.windowMode = value.mode;
                                    fullscreenDirty = true;
                                },
                                () -> WindowMode.fromValue(config.windowMode))
                                .setTranslator(value -> class_2561.method_43471(WindowMode.getComponentName(value))),
                        new RangeOption(class_2561.method_43471("options.framerateLimit"),
                                        10, 260, 10,
                                        value -> class_2561.method_30163(value == 260 ?
                                                                               class_2561.method_43471(
                                                                                                "options.framerateLimit.max")
                                                                                        .getString() :
                                                                               String.valueOf(value)),
                                        value -> {
                                            minecraftOptions.method_42524().method_41748(value);
                                            minecraft.method_61964().method_61938(value);
                                        },
                                        () -> minecraftOptions.method_42524().method_41753()),
                        new SwitchOption(class_2561.method_43471("options.vsync"),
                                         value -> {
                                             minecraftOptions.method_42433().method_41748(value);
                                             window.method_4497(value);
                                         },
                                         () -> minecraftOptions.method_42433().method_41753()),
                        new CyclingOption<>(class_2561.method_43471("options.inactivityFpsLimit"),
                                            class_9927.values(),
                                            value -> minecraftOptions.method_61970().method_41748(value),
                                            () -> minecraftOptions.method_61970().method_41753())
                                .setTranslator(class_9927::method_76526)
                }),
                new OptionBlock("", new Option<?>[]{
                        new RangeOption(class_2561.method_43471("options.guiScale"),
                                        0, window.method_4476(0, minecraft.method_1573()), 1,
                                        value -> class_2561.method_43471((value == 0)
                                                                                ? "options.guiScale.auto"
                                                                                : String.valueOf(value)),
                                        value -> {
                                            minecraftOptions.method_42474().method_41748(value);
                                            minecraft.method_15993();
                                        },
                                        () -> (minecraftOptions.method_42474().method_41753())),
                        new RangeOption(class_2561.method_43471("options.gamma"),
                                        0, 100, 1,
                                        value -> class_2561.method_43471(switch (value) {
                                            case 0 -> "options.gamma.min";
                                            case 50 -> "options.gamma.default";
                                            case 100 -> "options.gamma.max";
                                            default -> String.valueOf(value);
                                        }),
                                        value -> minecraftOptions.method_42473().method_41748(value * 0.01),
                                        () -> (int) (minecraftOptions.method_42473().method_41753() * 100.0)),
                }),
                new OptionBlock("", new Option<?>[]{
                        new SwitchOption(class_2561.method_43471("options.viewBobbing"),
                                         (value) -> minecraftOptions.method_42448().method_41748(value),
                                         () -> minecraftOptions.method_42448().method_41753()),
                        new CyclingOption<>(class_2561.method_43471("options.attackIndicator"),
                                            class_4061.values(),
                                            value -> minecraftOptions.method_42565().method_41748(value),
                                            () -> minecraftOptions.method_42565().method_41753())
                                .setTranslator(class_4061::method_76522),
                        new SwitchOption(class_2561.method_43471("options.autosaveIndicator"),
                                         value -> minecraftOptions.method_42452().method_41748(value),
                                         () -> minecraftOptions.method_42452().method_41753()),
                })
        };
    }

    public static OptionBlock[] getGraphicsOpts() {
        var texFilteringOption = new CyclingOption<>(class_2561.method_43471("options.textureFiltering"),
                                                     class_12393.values(),
                                                     value -> {
                                                         var oldValue = minecraftOptions.method_76747()
                                                                                        .method_41753();

                                                         if ((oldValue == class_12393.field_64665 && value != class_12393.field_64665)
                                                             || (value == class_12393.field_64665 && oldValue != class_12393.field_64665)) {
                                                             minecraft.method_1513();
                                                             WorldRenderer.getInstance()
                                                                          .resetSampler();
                                                         }

                                                         minecraftOptions.method_76747()
                                                                         .method_41748(value);
                                                     },
                                                     () -> minecraftOptions.method_76747()
                                                                           .method_41753())
                .setTranslator(class_12393::method_76751);

        var maxAnisotropyOption = new RangeOption(class_2561.method_43471("options.maxAnisotropy"),
                                                  1, 3, 1,
                                                  value -> {
                                                      var oldValue = minecraftOptions.method_76247()
                                                                                     .method_41753();

                                                      if (minecraftOptions.method_76747().method_41753() == class_12393.field_64665
                                                          && !oldValue.equals(value)) {
                                                          minecraft.method_1513();
                                                          WorldRenderer.getInstance()
                                                                       .resetSampler();
                                                      }

                                                      minecraftOptions.method_76247()
                                                                      .method_41748(value);
                                                  },
                                                  () -> minecraftOptions.method_76247()
                                                                        .method_41753())
                .setTranslator((value) -> class_2561.method_43469("options.multiplier", Integer.toString(1 << value)));

        maxAnisotropyOption.setActivationFn(() -> texFilteringOption.getNewValue() == class_12393.field_64665);
        texFilteringOption.setOnChange(maxAnisotropyOption::updateActiveState);

        return new OptionBlock[]{
                new OptionBlock("", new Option<?>[]{
                        new RangeOption(class_2561.method_43471("options.renderDistance"),
                                        2, 32, 1,
                                        (value) -> minecraftOptions.method_42503().method_41748(value),
                                        () -> minecraftOptions.method_42503().method_41753()),
                        new RangeOption(class_2561.method_43471("options.simulationDistance"),
                                        5, 32, 1,
                                        (value) -> minecraftOptions.method_42510().method_41748(value),
                                        () -> minecraftOptions.method_42510().method_41753()),
                        new CyclingOption<>(class_2561.method_43471("options.prioritizeChunkUpdates"),
                                            class_6597.values(),
                                            value -> minecraftOptions.method_41798().method_41748(value),
                                            () -> minecraftOptions.method_41798().method_41753())
                                .setTranslator(class_6597::method_76535),
                }),
                new OptionBlock("", new Option<?>[]{
                        new CyclingOption<>(class_2561.method_43471("options.graphics.preset"),
                                            new class_5365[]{class_5365.field_25427, class_5365.field_25428, class_5365.field_63461},
                                            value -> minecraftOptions.method_75329().method_41748(value),
                                            () -> minecraftOptions.method_75329().method_41753())
                                .setTranslator(graphicsMode -> class_2561.method_43471(graphicsMode.method_75305())),
                        texFilteringOption,
                        maxAnisotropyOption,
                        new CyclingOption<>(class_2561.method_43471("options.particles"),
                                            new class_4066[]{class_4066.field_18199, class_4066.field_18198, class_4066.field_18197},
                                            value -> minecraftOptions.method_42475().method_41748(value),
                                            () -> minecraftOptions.method_42475().method_41753())
                                .setTranslator(class_4066::method_76329),
                        new CyclingOption<>(class_2561.method_43471("options.renderClouds"),
                                            class_4063.values(),
                                            value -> minecraftOptions.method_42528().method_41748(value),
                                            () -> minecraftOptions.method_42528().method_41753())
                                .setTranslator(class_4063::method_76525),
                        new RangeOption(class_2561.method_43471("options.renderCloudsDistance"),
                                        2, 128, 1,
                                        (value) -> minecraftOptions.method_71270().method_41748(value),
                                        () -> minecraftOptions.method_71270().method_41753()),
                        new SwitchOption(class_2561.method_43471("options.cutoutLeaves"),
                                         value -> minecraftOptions.method_75334().method_41748(value),
                                         () -> minecraftOptions.method_75334().method_41753())
                                .setTooltip(class_2561.method_43471("options.cutoutLeaves.tooltip")),
                        new RangeOption(class_2561.method_43471("options.chunkFade"),
                                        0, 40, 1,
                                        (value) -> minecraftOptions.method_76253().method_41748(value / 20.0),
                                        () -> (int) (minecraftOptions.method_76253().method_41753() * 20))
                                .setTranslator(value -> class_2561.method_43470(String.valueOf(value / 20.0f))),
                        // TODO: improved transparency
//                        new SwitchOption(Component.translatable("options.improvedTransparency"),
//                                         value -> minecraftOptions.improvedTransparency().set(value),
//                                         () -> minecraftOptions.improvedTransparency().get())
//                                .setTooltip(Component.translatable("options.improvedTransparency.tooltip")),
                        new CyclingOption<>(class_2561.method_43471("options.ao"),
                                            new Integer[]{LightMode.FLAT, LightMode.SMOOTH, LightMode.SUB_BLOCK},
                                            (value) -> {
                                                if (value > LightMode.FLAT)
                                                    minecraftOptions.method_41792().method_41748(true);
                                                else
                                                    minecraftOptions.method_41792().method_41748(false);

                                                config.ambientOcclusion = value;

                                                minecraft.field_1769.method_3279();
                                            },
                                            () -> config.ambientOcclusion)
                                .setTranslator(value -> class_2561.method_43471(switch (value) {
                                    case LightMode.FLAT -> "options.off";
                                    case LightMode.SMOOTH -> "options.on";
                                    case LightMode.SUB_BLOCK -> "vulkanmod.options.ao.subBlock";
                                    default -> "vulkanmod.options.unknown";
                                }))
                                .setTooltip(class_2561.method_43471("vulkanmod.options.ao.subBlock.tooltip")),
                        new RangeOption(class_2561.method_43471("options.biomeBlendRadius"),
                                        0, 7, 1,
                                        value -> {
                                            int v = value * 2 + 1;
                                            return class_2561.method_30163("%d x %d".formatted(v, v));
                                        },
                                        (value) -> {
                                            minecraftOptions.method_41805().method_41748(value);
                                            minecraft.field_1769.method_3279();
                                        },
                                        () -> minecraftOptions.method_41805().method_41753()),
                }),
                new OptionBlock("", new Option<?>[]{
                        new SwitchOption(class_2561.method_43471("options.entityShadows"),
                                         value -> minecraftOptions.method_42435().method_41748(value),
                                         () -> minecraftOptions.method_42435().method_41753()),
                        new RangeOption(class_2561.method_43471("options.entityDistanceScaling"),
                                        2, 20, 1,
                                        value -> minecraftOptions.method_42517().method_41748(value / 4.0),
                                        () -> (int) (minecraftOptions.method_42517().method_41753() * 4.0))
                                        .setTranslator(value -> class_2561.method_43470(String.valueOf(value / 4.0))),
                        new CyclingOption<>(class_2561.method_43471("options.mipmapLevels"),
                                            new Integer[]{0, 1, 2, 3, 4},
                                            value -> {
                                                minecraftOptions.method_42563().method_41748(value);
                                                minecraft.method_24041(value);
                                                minecraft.method_1513();
                                            },
                                            () -> minecraftOptions.method_42563().method_41753())
                                .setTranslator(value -> class_2561.method_30163(value.toString())),
                        new RangeOption(class_2561.method_43471("options.weatherRadius"),
                                        3, 10, 1,
                                        value -> minecraftOptions.method_75333().method_41748(value),
                                        () -> minecraftOptions.method_75333().method_41753())
                                .setTooltip(class_2561.method_43471("options.weatherRadius.tooltip")),
                        new SwitchOption(class_2561.method_43471("options.vignette"),
                                         value -> minecraftOptions.method_75335().method_41748(value),
                                         () -> minecraftOptions.method_75335().method_41753())
                                .setTooltip(class_2561.method_43471("options.vignette.tooltip")),
                })
        };
    }

    public static OptionBlock[] getOptimizationOpts() {
        return new OptionBlock[]{
                new OptionBlock("", new Option[]{
                        new CyclingOption<>(class_2561.method_43471("vulkanmod.options.advCulling"),
                                            new Integer[]{1, 2, 3, 10},
                                            value -> config.advCulling = value,
                                            () -> config.advCulling)
                                .setTranslator(value -> class_2561.method_43471(switch (value) {
                                    case 1 -> "vulkanmod.options.advCulling.aggressive";
                                    case 2 -> "vulkanmod.options.advCulling.normal";
                                    case 3 -> "vulkanmod.options.advCulling.conservative";
                                    case 10 -> "options.off";
                                    default -> "vulkanmod.options.unknown";
                                }))
                                .setTooltip(class_2561.method_43471("vulkanmod.options.advCulling.tooltip")),
                        new SwitchOption(class_2561.method_43471("vulkanmod.options.entityCulling"),
                                         value -> config.entityCulling = value,
                                         () -> config.entityCulling)
                                .setTooltip(class_2561.method_43471("vulkanmod.options.entityCulling.tooltip")),
                        new SwitchOption(class_2561.method_43471("vulkanmod.options.uniqueOpaqueLayer"),
                                         value -> {
                                             config.uniqueOpaqueLayer = value;
                                             TerrainRenderType.updateMapping();
                                             minecraft.field_1769.method_3279();
                                         },
                                         () -> config.uniqueOpaqueLayer)
                                .setTooltip(class_2561.method_43471("vulkanmod.options.uniqueOpaqueLayer.tooltip")),
                        new SwitchOption(class_2561.method_43471("vulkanmod.options.backfaceCulling"),
                                         value -> {
                                             config.backFaceCulling = value;
                                             class_310.method_1551().field_1769.method_3279();
                                         },
                                         () -> config.backFaceCulling)
                                .setTooltip(class_2561.method_43471("vulkanmod.options.backfaceCulling.tooltip")),
                        new SwitchOption(class_2561.method_43471("vulkanmod.options.indirectDraw"),
                                         value -> config.indirectDraw = value,
                                         () -> config.indirectDraw)
                                .setTooltip(class_2561.method_43471("vulkanmod.options.indirectDraw.tooltip")),
                })
        };

    }

    public static OptionBlock[] getOtherOpts() {
        return new OptionBlock[]{
                new OptionBlock("", new Option[]{
                        new RangeOption(class_2561.method_43471("vulkanmod.options.builderThreads"),
                                        0, (Runtime.getRuntime().availableProcessors() - 1), 1,
                                        value -> {
                                            config.builderThreads = value;
                                            WorldRenderer.getInstance().getTaskDispatcher().createThreads(value);
                                        },
                                        () -> config.builderThreads)
                                .setTranslator(value -> {
                            if (value == 0)
                                return class_2561.method_43471("vulkanmod.options.builderThreads.auto");
                            else
                                return class_2561.method_30163(String.valueOf(value));
                        }),
                        new RangeOption(class_2561.method_43471("vulkanmod.options.frameQueue"),
                                        2, 5, 1,
                                        value -> {
                                            config.frameQueueSize = value;
                                            Renderer.scheduleSwapChainUpdate();
                                        }, () -> config.frameQueueSize)
                                .setTooltip(class_2561.method_43471("vulkanmod.options.frameQueue.tooltip")),
                        new SwitchOption(class_2561.method_43471("vulkanmod.options.textureAnimations"),
                                         value -> {
                                             config.textureAnimations = value;
                                         },
                                         () -> config.textureAnimations),
                }),
                new OptionBlock("", new Option[]{
                        new CyclingOption<>(class_2561.method_43471("vulkanmod.options.deviceSelector"),
                                            IntStream.range(-1, DeviceManager.suitableDevices.size()).boxed()
                                                     .toArray(Integer[]::new),
                                            value -> config.device = value,
                                            () -> config.device)
                                .setTranslator(value -> class_2561.method_43471((value == -1)
                                                                                       ? "vulkanmod.options.deviceSelector.auto"
                                                                                       : DeviceManager.suitableDevices.get(
                                        value).deviceName)
                                )
                                .setTooltip(class_2561.method_30163("%s: %s".formatted(
                                class_2561.method_43471("vulkanmod.options.deviceSelector.tooltip").getString(),
                                DeviceManager.device.deviceName)))
                })
        };

    }
}
