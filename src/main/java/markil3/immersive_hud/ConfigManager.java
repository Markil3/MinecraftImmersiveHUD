package markil3.immersive_hud;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.nio.file.Paths;

import static markil3.immersive_hud.Main.TICKS_PER_SECOND;

/**
 * Configuration manager of this mod, which reads from and writes to this mod's
 * configuration file.
 * <p>
 * The methods that change this mod's configuration do not automatically write
 * those changes to the configuration file on disk. Instead, they only update
 * the configuration in memory.
 *
 * @author Markil 3
 */
public class ConfigManager
{
    /**
     * The only instance of this class
     */
    private static final ConfigManager INSTANCE;

    /**
     * The {@link ForgeConfigSpec} instance for this mod's configuration
     */
    private static final ForgeConfigSpec SPEC;

    /**
     * {@link Path} to the configuration file of this mod
     */
    private static final Path CONFIG_PATH =
            Paths.get("config",
                    Main.class.getAnnotation(Mod.class).value() + ".toml");

    static
    {
        Pair<ConfigManager, ForgeConfigSpec> specPair =
                new ForgeConfigSpec.Builder().configure(ConfigManager::new);
        INSTANCE = specPair.getLeft();
        SPEC = specPair.getRight();
        CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH)
                .sync()
                .autoreload()
                .writingMode(WritingMode.REPLACE)
                .build();
        config.load();
        config.save();
        SPEC.setConfig(config);
    }

    public static class TimeValues
    {
        private static final int SHOW_TIME = 6 * TICKS_PER_SECOND;
        private static final int FADE_IN = TICKS_PER_SECOND / 4;
        private static final int FADE_OUT = TICKS_PER_SECOND / 2;

        private final String name;
        private final ForgeConfigSpec.IntValue maxTime;
        private final ForgeConfigSpec.IntValue fadeIn;
        private final ForgeConfigSpec.IntValue fadeOut;

        TimeValues(String name, ForgeConfigSpec.Builder configSpecBuilder)
        {
            final int MAX_TIME = 10 * 60 * TICKS_PER_SECOND;

            this.name = name;
            this.maxTime =
                    configSpecBuilder.translation("immersive_hud.configGui." + name + "Time.title")
                            .defineInRange(name + "Time.maxTime",
                                    SHOW_TIME,
                                    0,
                                    MAX_TIME);
            this.fadeIn =
                    configSpecBuilder.translation("immersive_hud.configGui." + name + "FadeIn.title")
                            .defineInRange(name + "Time.fadeIn",
                                    FADE_IN,
                                    0,
                                    MAX_TIME);
            this.fadeOut =
                    configSpecBuilder.translation("immersive_hud.configGui." + name + "FadeOut.title")
                            .defineInRange(name + "Time.fadeOut",
                                    FADE_OUT,
                                    0,
                                    MAX_TIME);
        }

        public String getName()
        {
            return this.name;
        }

        /**
         * Obtains the maximum time that the hotbar can be on the screen,
         * including fading.
         *
         * @return The maximum hotbar display time.
         */
        public int getMaxTime()
        {
            return this.maxTime.get();
        }

        /**
         * Obtains the time that the hotbar takes to fade in.
         *
         * @return The hotbar fade in time.
         */
        public int getFadeInTime()
        {
            return this.fadeIn.get();
        }

        /**
         * Obtains the time that the hotbar takes to fade out.
         *
         * @return The hotbar fade out time.
         */
        public int getFadeOutTime()
        {
            return this.fadeOut.get();
        }

//        public void setMaxTime(int time)
//        {
//            this.maxTime.set(time);
//        }
//
//        public void setFadeInTime(int time)
//        {
//            this.fadeIn.set(time);
//        }
//
//        public void setFadeOutTime(int time)
//        {
//            this.fadeOut.set(time);
//        }
    }

    private final TimeValues hotbarTime;
    private final TimeValues experienceTime;
    private final TimeValues jumpTime;
    private final TimeValues healthTime;
    private final TimeValues hungerTime;
    private final TimeValues effectTime;
    private final ForgeConfigSpec.IntValue crosshairTime;
    private final ForgeConfigSpec.BooleanValue hideCrosshair;
    private final ForgeConfigSpec.IntValue handTime;
    private final ForgeConfigSpec.BooleanValue hideHands;
    private final ForgeConfigSpec.BooleanValue showArmor;
    private final ForgeConfigSpec.DoubleValue minHealth;
    private final ForgeConfigSpec.IntValue minHunger;

    /**
     * Implementation of Singleton design pattern, which allows only one
     * instance of this class to be created.
     */
    private ConfigManager(ForgeConfigSpec.Builder configSpecBuilder)
    {
        // Comments are not added because there was no way to translate
        // descriptions from translate keys here

        this.hotbarTime = new TimeValues("hotbar", configSpecBuilder);
        this.experienceTime = new TimeValues("experience", configSpecBuilder);
        this.jumpTime = new TimeValues("jump", configSpecBuilder);
        this.healthTime = new TimeValues("health", configSpecBuilder);
        this.hungerTime = new TimeValues("hunger", configSpecBuilder);
        this.effectTime = new TimeValues("effect", configSpecBuilder);
        this.crosshairTime =
                configSpecBuilder.translation(
                        "immersive_hud.configGui.crosshairTime.title")
                        .defineInRange("crosshairTime",
                                6 * TICKS_PER_SECOND,
                                0,
                                10 * 60 * TICKS_PER_SECOND);
        this.hideCrosshair =
                configSpecBuilder.translation(
                        "immersive_hud.configGui.hideCrosshair.title")
                        .define("hideCrosshair", true);
        this.handTime =
                configSpecBuilder.translation(
                        "immersive_hud.configGui.handTime.title")
                        .defineInRange("handTime",
                                30 * TICKS_PER_SECOND,
                                0,
                                10 * 60 * TICKS_PER_SECOND);
        this.hideHands =
                configSpecBuilder.translation(
                        "immersive_hud.configGui.hideHands.title")
                        .define("hideHands", true);
        this.showArmor = configSpecBuilder.translation(
                "immersive_hud.configGui.showArmor.title")
                .define("showArmor", true);
        this.minHealth = configSpecBuilder.translation(
                "immersive_hud.configGui.minHealth.title")
                .defineInRange("minHealth", 0.5, 0.0, 1.0);
        this.minHunger = configSpecBuilder.translation(
                "immersive_hud.configGui.minHunger.title")
                .defineInRange("minHunger", 17, 0, 20);
    }

    /**
     * Returns the instance of this class.
     *
     * @return the instance of this class
     */
    public static ConfigManager getInstance()
    {
        return INSTANCE;
    }

    // Validations

    /**
     * Obtains time values related to the hotbar.
     *
     * @return The hotbar display time values.
     */
    public TimeValues getHotbarTime()
    {
        return this.hotbarTime;
    }

    /**
     * Obtains time values related to the experience bar.
     *
     * @return The experience bar display time values.
     */
    public TimeValues getExperenceTime()
    {
        return this.experienceTime;
    }

    /**
     * Obtains time values related to the jump bar.
     *
     * @return The jump display time values.
     */
    public TimeValues getJumpTime()
    {
        return this.jumpTime;
    }

    /**
     * Obtains time values related to the health bar.
     *
     * @return The health display time values.
     */
    public TimeValues getHealthTime()
    {
        return this.healthTime;
    }

    /**
     * Obtains time values related to the hunger bar.
     *
     * @return The hunger display time values.
     */
    public TimeValues getHungerTime()
    {
        return this.hungerTime;
    }

    /**
     * Obtains time values related to potion effects.
     *
     * @return Potion display time values.
     */
    public TimeValues getPotionTime()
    {
        return this.effectTime;
    }

    /**
     * Obtains the time that the crosshairs are allowed to display.
     *
     * @return The crosshair display time values.
     */
    public int getCrosshairTime()
    {
        return this.crosshairTime.get();
    }

    /**
     * Checks whether the crosshairs should be hidden after a period of time.
     *
     * @return - Whether or not crosshairs will be hidden.
     */
    public boolean hideCrosshair()
    {
        return this.hideCrosshair.get();
    }

    /**
     * Obtains the time that the hands are allowed to display.
     *
     * @return The hand display time values.
     */
    public int getHandTime()
    {
        return this.handTime.get();
    }

    /**
     * Checks whether hands should be hidden after a period of time.
     *
     * @return - Whether or not hands will be hidden.
     */
    public boolean hideHands()
    {
        return this.hideHands.get();
    }

    /**
     * Checks whether the armor bar should render.
     *
     * @return Whether or not armor shows on the HUD.
     */
    public boolean shouldShowArmor()
    {
        return this.showArmor.get();
    }

    /**
     * Obtains the minimum health for fading. Anything below that and the health
     * always displays.
     *
     * @return The health boundary, from 0 to 1.
     */
    public double getMinHealth()
    {
        return this.minHealth.get();
    }

    /**
     * Obtains the minimum hunger for fading. Anything below that and the hunger
     * always displays.
     *
     * @return The hunger boundary, from 0 to 20.
     */
    public int getMinHunger()
    {
        return this.minHunger.get();
    }

//    /**
//     * Sets the time that the hands are allowed to display.
//     *
//     * @param time - The hand display time values.
//     */
//    public void setCrosshairTime(int time)
//    {
//        this.crosshairTime.set(time);
//    }
//
//    /**
//     * Sets whether the crosshairs should be hidden after a period of time.
//     *
//     * @param hide - Whether or not crosshairs will be hidden.
//     */
//    public void hideCrosshair(boolean hide)
//    {
//        this.hideCrosshair.set(hide);
//    }
//
//    /**
//     * Sets the time that the hands are allowed to display.
//     *
//     * @param time - The hand display time values.
//     */
//    public void setHandTime(int time)
//    {
//        this.handTime.set(time);
//    }
//
//    /**
//     * Sets whether hands should be hidden after a period of time.
//     *
//     * @param hide - Whether or not hands will be hidden.
//     */
//    public void hideHands(boolean hide)
//    {
//        this.hideHands.set(hide);
//    }
//
//    /**
//     * Determines whether the armor bar should render.
//     *
//     * @param show - Whether or not armor shows on the HUD.
//     */
//    public void shouldShowArmor(boolean show)
//    {
//        this.showArmor.set(show);
//    }
//
//    /**
//     * Sets the minimum health for fading. Anything below that and the health
//     * always displays.
//     *
//     * @param boundary - The health boundary, from 0 to 1.
//     */
//    public void setMinHealth(double boundary)
//    {
//        this.minHealth.set(MathHelper.clamp(boundary, 0, 1));
//    }
//
//    /**
//     * Sets the minimum hunger for fading. Anything below that and the hunger
//     * always displays.
//     *
//     * @param boundary - The hunger boundary, from 0 to 20.
//     */
//    public void setMinHunger(int boundary)
//    {
//        this.minHunger.set(MathHelper.clamp(boundary, 0, 20));
//    }

    /**
     * Saves changes to this mod's configuration.
     */
//    public void save()
//    {
//        SPEC.save();
//    }
}
