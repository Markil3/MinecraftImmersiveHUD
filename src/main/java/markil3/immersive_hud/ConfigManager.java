package markil3.immersive_hud;

import net.minecraft.util.math.MathHelper;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static markil3.immersive_hud.Main.TICKS_PER_SECOND;

/**
 * Configuration manager of this mod, which reads from and writes to this mod's
 * configuration file.
 * <p>
 * The methods that change this mod's configuration do not automatically write
 * those changes to the configuration file on disk. Instead, they only update
 * the configuration in memory. To write any changes, use the {@link #save()}
 * method.
 *
 * @author Markil 3
 */
@Config(name = "immersive_hud")
public class ConfigManager implements ConfigData
{
    public static class TimeValues
    {
        private static final int SHOW_TIME = 6 * TICKS_PER_SECOND;
        private static final int FADE_IN = TICKS_PER_SECOND / 2;
        private static final int FADE_OUT = TICKS_PER_SECOND;

        private int maxTime = SHOW_TIME;
        private int fadeIn = FADE_IN;
        private int fadeOut = FADE_OUT;

        /**
         * Obtains the maximum time that the hotbar can be on the screen,
         * including fading.
         *
         * @return The maximum hotbar display time.
         */
        public int getMaxTime()
        {
            return this.maxTime;
        }

        /**
         * Obtains the time that the hotbar takes to fade in.
         *
         * @return The hotbar fade in time.
         */
        public int getFadeInTime()
        {
            return this.fadeIn;
        }

        /**
         * Obtains the time that the hotbar takes to fade out.
         *
         * @return The hotbar fade out time.
         */
        public int getFadeOutTime()
        {
            return this.fadeOut;
        }

        public void setMaxTime(int time)
        {
            this.maxTime = time;
        }

        public void setFadeInTime(int time)
        {
            this.fadeIn = time;
        }

        public void setFadeOutTime(int time)
        {
            this.fadeOut = time;
        }
    }

    private TimeValues hotbarTime = new TimeValues();
    private TimeValues experienceTime = new TimeValues();
    private TimeValues jumpTime = new TimeValues();
    private TimeValues healthTime = new TimeValues();
    private TimeValues hungerTime = new TimeValues();
    private TimeValues effectTime = new TimeValues();
    private int crosshairTime = 6 * TICKS_PER_SECOND;
    private int handTime = 30 * TICKS_PER_SECOND;
    private boolean showArmor = true;
    private double minHealth = 0.5;
    @ConfigEntry.BoundedDiscrete(max = 20)
    private int minHunger = 17;

    /**
     * Returns the instance of this class.
     *
     * @return the instance of this class
     */
    public static ConfigManager getInstance()
    {
        return AutoConfig.getConfigHolder(ConfigManager.class).getConfig();
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
    public TimeValues getExperienceTime()
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
        return this.crosshairTime;
    }

    /**
     * Obtains the time that the hands are allowed to display.
     *
     * @return The hand display time values.
     */
    public int getHandTime()
    {
        return this.handTime;
    }

    /**
     * Checks whether the armor bar should render.
     *
     * @return Whether or not armor shows on the HUD.
     */
    public boolean shouldShowArmor()
    {
        return this.showArmor;
    }

    /**
     * Obtains the minimum health for fading. Anything below that and the health
     * always displays.
     *
     * @return The health boundary, from 0 to 1.
     */
    public double getMinHealth()
    {
        return this.minHealth;
    }

    /**
     * Obtains the minimum hunger for fading. Anything below that and the hunger
     * always displays.
     *
     * @return The hunger boundary, from 0 to 20.
     */
    public int getMinHunger()
    {
        return this.minHunger;
    }

    /**
     * Sets the time that the hands are allowed to display.
     *
     * @param time - The hand display time values.
     */
    public void setCrosshairTime(int time)
    {
        this.crosshairTime = time;
    }

    /**
     * Sets the time that the hands are allowed to display.
     *
     * @param time - The hand display time values.
     */
    public void setHandTime(int time)
    {
        this.handTime = time;
    }

    /**
     * Determines whether the armor bar should render.
     *
     * @param show - Whether or not armor shows on the HUD.
     */
    public void shouldShowArmor(boolean show)
    {
        this.showArmor = show;
    }

    /**
     * Sets the minimum health for fading. Anything below that and the health
     * always displays.
     *
     * @param boundary - The health boundary, from 0 to 1.
     */
    public void setMinHealth(double boundary)
    {
        this.minHealth = MathHelper.clamp(boundary, 0, 1);
    }

    /**
     * Sets the minimum hunger for fading. Anything below that and the hunger
     * always displays.
     *
     * @param boundary - The hunger boundary, from 0 to 20.
     */
    public void setMinHunger(int boundary)
    {
        this.minHunger = MathHelper.clamp(boundary, 0, 20);
    }

    @Override
    public void validatePostLoad() throws ValidationException
    {
        this.setMinHealth(this.minHealth);
    }
/**
     * Saves changes to this mod's configuration.
     */
//    public void save()
//    {
//        SPEC.save();
//    }
}
