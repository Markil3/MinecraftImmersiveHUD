package markil3.immersive_hud;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
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
                entryBuilder.startDoubleField("option.immersive_hud." + name +
                                "MaxTime",
                        (float) value.getMaxTime() / TICKS_PER_SECOND)
                        .setDefaultValue(SHOW_TIME)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND) // 10 Minutes
                        .setTooltip("tooltip.immersive_hud." + name +
                                "MaxTime")
                        .setSaveConsumer(val -> value.setMaxTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        DoubleListEntry fadeIn =
                entryBuilder.startDoubleField("option.immersive_hud." + name +
                                "FadeIn",
                        (float) value.getFadeInTime() / TICKS_PER_SECOND)
                        .setDefaultValue(FADE_IN)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND)
                        .setTooltip("tooltip.immersive_hud." + name +
                                "FadeIn")
                        .setSaveConsumer(val -> value.setFadeInTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        DoubleListEntry fadeOut =
                entryBuilder.startDoubleField("option.immersive_hud." + name +
                                "FadeOut",
                        (float) value.getFadeOutTime() / TICKS_PER_SECOND)
                        .setDefaultValue(FADE_OUT)
                        .setMin(0)
                        .setMax(10 * 60 * TICKS_PER_SECOND)
                        .setTooltip("tooltip.immersive_hud." + name +
                                "FadeOut")
                        .setSaveConsumer(val -> value.setFadeOutTime((int) (val * TICKS_PER_SECOND)))
                        .build();

        cat.addEntry(maxTime);
        cat.addEntry(fadeIn);
        cat.addEntry(fadeOut);
    }

    @Override
    public String getModId()
    {
        return "immersive_hud";
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory()
    {
        return screen -> {
            ConfigCategory general;
            ConfigEntryBuilder entryBuilder;
            ConfigManager instance = ConfigManager.getInstance();
            final ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(screen)
                    .setTitle("immersive_hud.configGui.title")
                    .setSavingRunnable(() -> {
                        try
                        {
                            if (Class.forName(
                                    "me.sargunvohra.mcmods.autoconfig1u" +
                                            ".AutoConfig") != null)
                            {
                                ((me.sargunvohra.mcmods.autoconfig1u.ConfigManager<ConfigManagerCloth>) AutoConfig
                                        .getConfigHolder(
                                                ConfigManagerCloth.class)).save();
                            }
                        }
                        catch (ClassNotFoundException e)
                        {
                            // Do nothing
                        }
                    });
            general =
                    builder.getOrCreateCategory("category.immersive_hud" +
                            ".general");
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

            general.addEntry(entryBuilder.startDoubleField(
                    "option.immersive_hud.crosshairTime",
                    (float) instance.getCrosshairTime() / TICKS_PER_SECOND)
                    .setDefaultValue(6)
                    .setTooltip("tooltip.immersive_hud.crosshairTime")
                    .setSaveConsumer(val -> instance.setCrosshairTime((int) (val * TICKS_PER_SECOND)))
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            "option.immersive_hud.hideCrosshair",
                    instance.hideCrosshair())
                    .setDefaultValue(true)
                    .setTooltip("tooltip.immersive_hud.hideCrosshair")
                    .setSaveConsumer(val -> instance.hideCrosshair(val))
                    .build());

            general.addEntry(entryBuilder.startDoubleField(
                    "option.immersive_hud.handTime",
                    (float) instance.getHandTime() / TICKS_PER_SECOND)
                    .setDefaultValue(30)
                    .setTooltip("tooltip.immersive_hud.handTime")
                    .setSaveConsumer(val -> instance.setHandTime((int) (val * TICKS_PER_SECOND)))
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                    "option.immersive_hud.hideHands",
                    instance.hideHands())
                    .setDefaultValue(true)
                    .setTooltip("tooltip.immersive_hud.hideHands")
                    .setSaveConsumer(val -> instance.hideHands(val))
                    .build());

            general.addEntry(entryBuilder.startDoubleField(
                    "option.immersive_hud.minHealth",
                    instance.getMinHealth())
                    .setDefaultValue(0.5)
                    .setTooltip("tooltip.immersive_hud.minHealth")
                    .setSaveConsumer(val -> instance.setMinHealth(val))
                    .build());

            general.addEntry(entryBuilder.startIntField(
                    "option.immersive_hud.minHunger",
                    instance.getMinHunger())
                    .setDefaultValue(17)
                    .setTooltip("tooltip.immersive_hud.minHunger")
                    .setSaveConsumer(val -> instance.setMinHunger(val))
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                    "option.immersive_hud.showArmor",
                    instance.shouldShowArmor())
                    .setDefaultValue(true)
                    .setTooltip("tooltip.immersive_hud.showArmor")
                    .setSaveConsumer(val -> instance.shouldShowArmor(val))
                    .build());

            return builder.build();
        };
    }
}
