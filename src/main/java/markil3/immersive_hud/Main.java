/**
 * This Minecraft mod aims to move the ingame HUD out of the way whenever
 * possible. Copyright (C) 2021 Markil 3
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package markil3.immersive_hud;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;

/**
 * The main entry point for the Immersive HUD mod.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-forge
 */
@Mod("immersive_hud")
public class Main
{
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int TICKS_PER_SECOND = 20;

    public Main()
    {
        FMLJavaModLoadingContext.get()
                .getModEventBus()
                .addListener(this::setup);

        setupScreen();
        //Make sure the mod being absent on the other network side does not
        // cause the client to display the server as incompatible
        ModLoadingContext.get()
                .registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair
                        .of(() -> FMLNetworkConstants.IGNORESERVERONLY,
                                (a, b) -> true));
    }

    private void startTimeField(ConfigEntryBuilder entryBuilder,
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

    private void setupScreen()
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

    private void setup(final FMLClientSetupEvent event)
    {
    }

    /**
     * A utility method to handle the transparency timing. It will (for the most
     * part), make
     *
     * @param renderTime - How many seconds until the element entirely
     * disappears.
     * @param maxTime - How many seconds the element can appear.
     *
     * @return The proper transparency to render something.
     */
    static float getAlpha(double renderTime,
                          double maxTime,
                          double fadeInTime,
                          double fadeOutTime)
    {
        if (renderTime <= fadeOutTime)
        {
            return (float) (renderTime / fadeOutTime);
        }
        else if (renderTime > maxTime - fadeInTime)
        {
            return (float) ((maxTime - renderTime) / fadeInTime);
        }
        return 1F;
    }
}
