package markil3.immersive_hud;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;

import static markil3.immersive_hud.Main.TICKS_PER_SECOND;

public class ModMenu
{

    static void startTimeField(ConfigEntryBuilder entryBuilder,
                                ConfigCategory cat,
                                ConfigManager.TimeValues value)
    {
        final float SHOW_TIME = 6;
        final float FADE_IN = 0.25F;
        final float FADE_OUT = 0.5F;

        DoubleListEntry maxTime =
                entryBuilder.startDoubleField(new TranslationTextComponent(
                                "option.immersive_hud." + value.getName() +
                                        "MaxTime"),
                        (float) value.getMaxTime() / TICKS_PER_SECOND)
                        .setDefaultValue(SHOW_TIME)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND) // 10 Minutes
                        .setTooltip(new TranslationTextComponent(
                                "tooltip.immersive_hud." + value.getName() +
                                        "MaxTime"))
                        .setSaveConsumer(val -> value.setMaxTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        DoubleListEntry fadeIn =
                entryBuilder.startDoubleField(new TranslationTextComponent(
                                "option.immersive_hud." + value.getName() +
                                        "FadeIn"),
                        (float) value.getFadeInTime() / TICKS_PER_SECOND)
                        .setDefaultValue(FADE_IN).setMin(0).setMax(10 * 60 * TICKS_PER_SECOND)
                        .setTooltip(new TranslationTextComponent(
                                "tooltip.immersive_hud." + value.getName() +
                                        "FadeIn"))
                        .setSaveConsumer(val -> value.setFadeInTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        DoubleListEntry fadeOut =
                entryBuilder.startDoubleField(new TranslationTextComponent(
                                "option.immersive_hud." + value.getName() +
                                        "FadeOut"),
                        (float) value.getFadeOutTime() / TICKS_PER_SECOND)
                        .setDefaultValue(FADE_OUT)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND)
                        .setTooltip(new TranslationTextComponent(
                                "tooltip.immersive_hud." + value.getName() +
                                        "FadeOut"))
                        .setSaveConsumer(val -> value.setFadeOutTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        cat.addEntry(maxTime);
        cat.addEntry(fadeIn);
        cat.addEntry(fadeOut);
    }

    static void setupScreen()
    {
        ModLoadingContext.get()
                .registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                        () -> (mc, screen) -> {
                            ConfigCategory general;
                            ConfigEntryBuilder entryBuilder;
                            final ConfigBuilder builder = ConfigBuilder.create()
                                    .setParentScreen(screen)
                                    .setTitle(new TranslationTextComponent(
                                            "immersive_hud.configGui.title"))
                                    .setSavingRunnable(() -> {
                                        ConfigManager.getInstance().save();
                                    });
                            general =
                                    builder.getOrCreateCategory(new TranslationTextComponent(
                                            "category.immersive_hud.general"));
                            entryBuilder = builder.entryBuilder();

                            startTimeField(entryBuilder,
                                    general,
                                    ConfigManager.getInstance()
                                            .getHotbarTime());
                            startTimeField(entryBuilder,
                                    general,
                                    ConfigManager.getInstance()
                                            .getExperenceTime());
                            startTimeField(entryBuilder,
                                    general,
                                    ConfigManager.getInstance().getJumpTime());
                            startTimeField(entryBuilder,
                                    general,
                                    ConfigManager.getInstance()
                                            .getHealthTime());
                            startTimeField(entryBuilder,
                                    general,
                                    ConfigManager.getInstance()
                                            .getHungerTime());
                            startTimeField(entryBuilder,
                                    general,
                                    ConfigManager.getInstance()
                                            .getPotionTime());

                            general.addEntry(entryBuilder.startDoubleField(new TranslationTextComponent(
                                            "option.immersive_hud" +
                                                    ".crosshairTime"),
                                    (float) ConfigManager.getInstance()
                                            .getCrosshairTime() / TICKS_PER_SECOND)
                                    .setDefaultValue(6)
                                    .setTooltip(new TranslationTextComponent(
                                            "tooltip.immersive_hud" +
                                                    ".crosshairTime"))
                                    .setSaveConsumer(val -> ConfigManager.getInstance()
                                            .setCrosshairTime((int) (val * TICKS_PER_SECOND)))
                                    .build());

                            general.addEntry(entryBuilder.startDoubleField(new TranslationTextComponent(
                                            "option.immersive_hud.handTime"),
                                    (float) ConfigManager.getInstance()
                                            .getHandTime() / TICKS_PER_SECOND)
                                    .setDefaultValue(30)
                                    .setTooltip(new TranslationTextComponent(
                                            "tooltip.immersive_hud.handTime"))
                                    .setSaveConsumer(val -> ConfigManager.getInstance()
                                            .setHandTime((int) (val * TICKS_PER_SECOND)))
                                    .build());

                            general.addEntry(entryBuilder.startDoubleField(new TranslationTextComponent(
                                            "option.immersive_hud.minHealth"),
                                    ConfigManager.getInstance()
                                            .getMinHealth())
                                    .setDefaultValue(0.5)
                                    .setTooltip(new TranslationTextComponent(
                                            "tooltip.immersive_hud.minHealth"))
                                    .setSaveConsumer(val -> ConfigManager.getInstance()
                                            .setMinHealth(
                                                    val))
                                    .build());

                            general.addEntry(entryBuilder.startIntField(new TranslationTextComponent(
                                            "option.immersive_hud.minHunger"),
                                    ConfigManager.getInstance()
                                            .getMinHunger())
                                    .setDefaultValue(17)
                                    .setTooltip(new TranslationTextComponent(
                                            "tooltip.immersive_hud.minHunger"))
                                    .setSaveConsumer(val -> ConfigManager.getInstance()
                                            .setMinHunger(
                                                    val))
                                    .build());

                            general.addEntry(entryBuilder.startBooleanToggle(new TranslationTextComponent(
                                            "option.immersive_hud.showArmor"),
                                    ConfigManager.getInstance()
                                            .shouldShowArmor())
                                    .setDefaultValue(true)
                                    .setTooltip(new TranslationTextComponent(
                                            "tooltip.immersive_hud.showArmor"))
                                    .setSaveConsumer(val -> ConfigManager.getInstance()
                                            .shouldShowArmor(
                                                    val))
                                    .build());

                            return builder.build();
                        });
    }
}
