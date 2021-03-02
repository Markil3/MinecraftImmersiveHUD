package markil3.immersive_hud;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.text.TranslatableText;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;

import static markil3.immersive_hud.Main.TICKS_PER_SECOND;

public class ModMenu implements ModMenuApi
{

    private void startTimeField(String name, ConfigEntryBuilder entryBuilder,
                                ConfigCategory cat,
                                ConfigManager.TimeValues value)
    {
        final float SHOW_TIME = 6;
        final float FADE_IN = 0.25F;
        final float FADE_OUT = 0.5F;

        DoubleListEntry maxTime =
                entryBuilder.startDoubleField(new TranslatableText(
                                "option.immersive_hud." + name +
                                        "MaxTime"),
                        (float) value.getMaxTime() / TICKS_PER_SECOND)
                        .setDefaultValue(SHOW_TIME)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND) // 10 Minutes
                        .setTooltip(new TranslatableText(
                                "tooltip.immersive_hud." + name +
                                        "MaxTime"))
                        .setSaveConsumer(val -> value.setMaxTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        DoubleListEntry fadeIn =
                entryBuilder.startDoubleField(new TranslatableText(
                                "option.immersive_hud." + name +
                                        "FadeIn"),
                        (float) value.getFadeInTime() / TICKS_PER_SECOND)
                        .setDefaultValue(FADE_IN)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND)
                        .setTooltip(new TranslatableText(
                                "tooltip.immersive_hud." + name +
                                        "FadeIn"))
                        .setSaveConsumer(val -> value.setFadeInTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        DoubleListEntry fadeOut =
                entryBuilder.startDoubleField(new TranslatableText(
                                "option.immersive_hud." + name +
                                        "FadeOut"),
                        (float) value.getFadeOutTime() / TICKS_PER_SECOND)
                        .setDefaultValue(FADE_OUT)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND)
                        .setTooltip(new TranslatableText(
                                "tooltip.immersive_hud." + name +
                                        "FadeOut"))
                        .setSaveConsumer(val -> value.setFadeOutTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        cat.addEntry(maxTime);
        cat.addEntry(fadeIn);
        cat.addEntry(fadeOut);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return screen -> {
            ConfigCategory general;
            ConfigEntryBuilder entryBuilder;
            ConfigManager instance = ConfigManager.getInstance();
            final ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(screen)
                    .setTitle(new TranslatableText(
                            "immersive_hud.configGui.title"))
                    .setSavingRunnable(() -> {
                        try
                        {
                            if (Class.forName("me.shedaniel.autoconfig.AutoConfig") != null)
                            {
                                AutoConfig.getConfigHolder(ConfigManagerCloth.class)
                                        .save();
                            }
                        }
                        catch (ClassNotFoundException e)
                        {
                            // Do nothing
                        }
                    });
            general =
                    builder.getOrCreateCategory(new TranslatableText(
                            "category.immersive_hud.general"));
            entryBuilder = builder.entryBuilder();

            startTimeField("hotbar", entryBuilder,
                    general,
                    instance.getHotbarTime());
            startTimeField("experience", entryBuilder,
                    general,
                    instance.getExperienceTime());
            startTimeField("jump", entryBuilder,
                    general,
                    instance.getJumpTime());
            startTimeField("jump", entryBuilder,
                    general,
                    instance.getHealthTime());
            startTimeField("hunger", entryBuilder,
                    general,
                    instance.getHungerTime());
            startTimeField("effect", entryBuilder,
                    general,
                    instance.getPotionTime());

            general.addEntry(entryBuilder.startDoubleField(new TranslatableText(
                            "option.immersive_hud" +
                                    ".crosshairTime"),
                    (float) instance.getCrosshairTime() / TICKS_PER_SECOND)
                    .setDefaultValue(6)
                    .setTooltip(new TranslatableText(
                            "tooltip.immersive_hud" +
                                    ".crosshairTime"))
                    .setSaveConsumer(val -> instance.setCrosshairTime((int) (val * TICKS_PER_SECOND)))
                    .build());

            general.addEntry(entryBuilder.startDoubleField(new TranslatableText(
                            "option.immersive_hud.handTime"),
                    (float) instance.getHandTime() / TICKS_PER_SECOND)
                    .setDefaultValue(30)
                    .setTooltip(new TranslatableText(
                            "tooltip.immersive_hud.handTime"))
                    .setSaveConsumer(val -> instance.setHandTime((int) (val * TICKS_PER_SECOND)))
                    .build());

            general.addEntry(entryBuilder.startDoubleField(new TranslatableText(
                            "option.immersive_hud.minHealth"),
                    instance.getMinHealth())
                    .setDefaultValue(0.5)
                    .setTooltip(new TranslatableText(
                            "tooltip.immersive_hud.minHealth"))
                    .setSaveConsumer(val -> instance.setMinHealth(val))
                    .build());

            general.addEntry(entryBuilder.startIntField(new TranslatableText(
                            "option.immersive_hud.minHunger"),
                    instance.getMinHunger())
                    .setDefaultValue(17)
                    .setTooltip(new TranslatableText(
                            "tooltip.immersive_hud.minHunger"))
                    .setSaveConsumer(val -> instance.setMinHunger(val))
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText(
                            "option.immersive_hud.showArmor"),
                    instance.shouldShowArmor())
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText(
                            "tooltip.immersive_hud.showArmor"))
                    .setSaveConsumer(val -> instance.shouldShowArmor(val))
                    .build());

            return builder.build();
        };
    }
}
