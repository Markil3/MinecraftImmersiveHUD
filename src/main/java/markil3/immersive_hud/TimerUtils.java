package markil3.immersive_hud;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class TimerUtils
{
//    /**
//     * The number of ticks that the hand will be onscreen. Every tick is
//     1/20th
//     * of a second.
//     */
//    private static final int HAND_TIME = 10 * 20;
//    /**
//     * The number of ticks most elements will be onscreen. Every tick is
//     1/20th
//     * of a second.
//     */
//    static final int VISUAL_TIME = 6 * 20;
//    /**
//     * The number of ticks that the health and hunger bars will be onscreen.
//     * Every tick is 1/20th of a second.
//     */
//    private static final int HEALTH_TIME = (int) ((float) VISUAL_TIME * 2.5);
//    public static final int FADE_IN_TIME = 5;
//    public static final int FADE_OUT_TIME = 20;

    /**
     * How many more ticks the hand will be onscreen. Every tick is 1/20th of a
     * second.
     */
    private static double mainHandTime, offHandTime;

    /**
     * How many more ticks the crosshair will be onscreen. Every tick is 1/20th
     * of a second.
     */
    private static double crosshairTime;
    /**
     * If true, then the hand (and crosshairs) will be locked onto the screen
     * without disappearing until this flag is updated.
     */
    private static boolean mainHandLock, offHandLock;
    /**
     * A bitmask that will lock the corresponding hand without necessarily
     * showing the crosshairs. The first bit will be the main hand, and the
     * second will be the offhand.
     */
    private static int mapLock;

    private static HashMap<Effect, Double> effectTime = new HashMap<>();

    /**
     * How many more ticks the hotbar will be onscreen. Every tick is 1/20th of
     * a second.
     */
    static double hotbarTime;
    /**
     * The last hotbar slot that was selected. This is used to check for a
     * change in the hotbar slot.
     */
    private static int selectedHotbarSlot = -1;
    /**
     * The last item that was in this hand. This is used to check for a change
     * in what was equipped.
     */
    private static Item mainHandItem = null, offHandItem = null;

    /**
     * How many more ticks the health bar will be onscreen. Every tick is 1/20th
     * of a second.
     */
    private static double healthTime;
    /**
     * Records what the health was previously. Used to check for a change in the
     * health.
     * <p>
     * Note that the display of the armor is linked to this.
     */
    private static float health = -1;
    /**
     * Records what the maximum health was previously. Used to check for a
     * change in the health.
     */
    private static float maxHealth = -1;

    /**
     * How many more ticks the hunger bar will be onscreen. Every tick is 1/20th
     * of a second.
     */
    private static double hungerTime;
    /**
     * Records what the hunger level was previously. Used to check for a change
     * in hunger.
     */
    private static int hunger = -1;
    /**
     * Records whether or not there was food poisoning. Used to check for a
     * change in hunger.
     */
    private static boolean isFoodPoisoned = false;

    /**
     * How many more ticks the mount's health bar will be onscreen. Every tick
     * is 1/20th of a second.
     */
    private static double mountTime;
    /**
     * Records what the health of the mount was previously. Used to check for a
     * change in the health.
     */
    private static float mountHealth = -1;
    /**
     * Records what the maximum health of the mount was previously. Used to
     * check for a change in the health.
     */
    private static float mountMaxHealth = -1;

    /**
     * How many more ticks the mount's jump bar will be onscreen. Every tick is
     * 1/20th of a second.
     */
    static double jumpTime;

    /**
     * How many more ticks the experience bar will be onscreen. Every tick is
     * 1/20th of a second.
     */
    static double experienceTime;
    /**
     * Records what the experience was previously. Used to check for a change in
     * the health.
     */
    private static float experienceProgress = -1;

    /**
     * Sets the transparency level of the next drawn element.
     *
     * @param alpha - The transparency, from 0 to 1.
     */
    public static void setAlpha(float alpha)
    {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
    }

    /**
     * Resets the transparency level.
     *
     * @see #setAlpha(float)
     */
    public static void resetAlpha()
    {
        setAlpha(1.0F);
    }

    /**
     * Obtains the offset from the bottom of the screen that the hotbar
     * receives.
     *
     * @return The offset the hotbar uses.
     */
    public static int getHotbarTranslation()
    {
        ConfigManager.TimeValues hotbar =
                ConfigManager.getInstance().getHotbarTime();
        return (int) (22F * Main.getAlpha(hotbarTime,
                hotbar.getMaxTime(),
                hotbar.getFadeInTime(),
                hotbar.getFadeOutTime()));
    }


    /**
     * Obtains the offset from the bottom of the screen that the experience bar
     * receives.
     *
     * @return The offset the experience bar uses.
     */
    public static int getExperienceTranslation()
    {
        ConfigManager.TimeValues experience =
                ConfigManager.getInstance().getExperenceTime();
        return (int) ((getHotbarTranslation() + 10F) * Main.getAlpha(
                experienceTime,
                experience.getMaxTime(),
                experience.getFadeInTime(),
                experience.getFadeOutTime())) - 3;
    }


    /**
     * Obtains the offset from the bottom of the screen that the jump bar
     * receives.
     *
     * @return The offset the jump bar uses.
     */
    public static int getJumpTranslation()
    {
        ConfigManager.TimeValues jump =
                ConfigManager.getInstance().getJumpTime();
        return (int) ((getHotbarTranslation() + 10F) * Main.getAlpha(jumpTime,
                jump.getMaxTime(),
                jump.getFadeInTime(),
                jump.getFadeOutTime())) - 3;
    }


    /**
     * Obtains the offset that the status bars (health, hunger, etc.) gets from
     * their usual position.
     *
     * @return The offset.
     */
    public static float getHealthTranslation()
    {
        return 29 - Math.max(Math.max(getExperienceTranslation(),
                getJumpTranslation()), getHotbarTranslation());
    }

    public static void onClick(Hand hand, Item item)
    {
        ConfigManager.TimeValues health =
                ConfigManager.getInstance().getHealthTime();
        ConfigManager.TimeValues hunger =
                ConfigManager.getInstance().getHungerTime();
        double handTime = ConfigManager.getInstance().getHandTime();

        crosshairTime = ConfigManager.getInstance().getCrosshairTime();

        if (hand == Hand.MAIN_HAND)
        {
            mainHandTime = handTime;
        }
        else
        {
            offHandTime = handTime;
        }
        if (item != null)
        {
            if (item.isFood())
            {
                healthTime = health.getMaxTime() - (healthTime > 0 ?
                                                    health.getFadeInTime() :
                                                    0);
                hungerTime = hunger.getMaxTime() - (hungerTime > 0 ?
                                                    hunger.getFadeInTime() :
                                                    0);
            }
        }
    }

    /**
     * Run whenever a hand is rendered in 1st person. This brings the player
     * hands into and out of view as needed.
     *
     * @param hand - The hand being rendered.
     * @param ticks
     *
     * @return If true, then don't render this hand at all.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean onRenderHand(Hand hand,
                                       float ticks)
    {
        float HAND_UP_TIME = 20F;

        switch (hand)
        {
        case OFF_HAND:
            if (mainHandTime > 0 && mainHandTime < HAND_UP_TIME)
            {
                /*
                 * Undo the transformation of the previous hand
                 */
                GlStateManager.translatef(0,
                        (float) (1.0F * (HAND_UP_TIME - mainHandTime) / HAND_UP_TIME),
                        0);
            }
            if (offHandTime == 0 && !offHandLock)
            {
                return true;
            }
            else if (!offHandLock && (mapLock & 0b01) == 0)
            {
                if (offHandTime > 0)
                {
                    offHandTime -= ticks;
                }
                if (offHandTime < HAND_UP_TIME)
                {
                    GlStateManager.translatef(0,
                            (float) (-1.0F * (HAND_UP_TIME - offHandTime) / HAND_UP_TIME),
                            0);
                }
            }
            break;
        case MAIN_HAND:
            if (mainHandTime == 0 && !mainHandLock)
            {
                return true;
            }
            else if (!mainHandLock && (mapLock & 0b10) == 0)
            {
                if (mainHandTime > 0)
                {
                    mainHandTime -= ticks;
                }
                if (mainHandTime < HAND_UP_TIME)
                {
                    GlStateManager.translatef(0,
                            (float) (-1.0F * (HAND_UP_TIME - mainHandTime) / HAND_UP_TIME),
                            0);
                }
            }
            break;
        }
        return false;
    }

    /**
     * Resets the health of the mount.
     */
    public static void resetMountHealth()
    {
        ConfigManager.TimeValues health =
                ConfigManager.getInstance().getHealthTime();
        mountTime = health.getMaxTime() - (mountTime > 0 ?
                                           health.getFadeInTime() :
                                           0);
        jumpTime = 0;
    }

    /**
     * Determines whether or not to draw the crosshair, adjusting the alpha as
     * needed.
     *
     * @param ticks
     *
     * @return If true, then cancel drawing the crosshair.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawCrosshair(float ticks)
    {
        double CROSSHAIR_TIME = ConfigManager.getInstance().getCrosshairTime();

        Minecraft mc = Minecraft.getInstance();
        boolean changed = false;
        boolean canceled = false;

        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() != RayTraceResult.Type.MISS)
        {
            if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY)
            {
                if (mc.objectMouseOver.hitInfo == null || mc.objectMouseOver.hitInfo != mc.player
                        .getRidingEntity())
                {
                    changed = true;
                }
            }
            else
            {
                changed = true;
            }
        }
        if (changed || mainHandLock || offHandLock || crosshairTime > 0)
        {
            setAlpha(Main.getAlpha(crosshairTime > 0 ?
                                   crosshairTime :
                                   CROSSHAIR_TIME, CROSSHAIR_TIME, 0, 0));
        }
        else
        {
            canceled = true;
        }
        if (crosshairTime > 0)
        {
            crosshairTime -= ticks;
        }

        return canceled;
    }

    /**
     * Updates the timings of some of the potions currently affecting the
     * player.
     *
     * @param player - The player being affected.
     */
    public static void updatePotions(ClientPlayerEntity player)
    {
        ArrayList<Effect> toRemove = new ArrayList<>();
        for (Effect effect : Collections.checkedSet(effectTime.keySet(),
                Effect.class))
        {
            if (!player.isPotionActive(effect))
            {
                toRemove.add(effect);
            }
        }
        for (Effect effect : toRemove)
        {
            effectTime.remove(effect);
        }
    }

    /**
     * Obtains how transparent
     *
     * @param player
     * @param effectinstance
     *
     * @return
     */
    public static float getPotionAlpha(ClientPlayerEntity player,
                                       EffectInstance effectinstance,
                                       float ticks)
    {
        ConfigManager.TimeValues potion =
                ConfigManager.getInstance().getPotionTime();
        final int BLINK_TIME = 200;
        float effectAlpha = 0.0F;
        Double time = effectTime.get(effectinstance.getPotion());

        if (time == null)
        {
            effectTime.put(effectinstance.getPotion(),
                    (time = (double) potion.getMaxTime()));
        }
        else
        {
            effectTime.put(effectinstance.getPotion(), (time -= ticks));
        }
        if (effectinstance.getDuration() <= BLINK_TIME)
        {
            effectAlpha =
                    MathHelper.sin(7000F / (effectinstance.getDuration() + 16F * (float) Math.PI)) * 50F / (effectinstance
                            .getDuration() + 100F) + 0.5F;
        }
        else if (effectinstance.getDuration() <= BLINK_TIME + potion.getFadeInTime())
        {
            effectAlpha =
                    -(effectinstance.getDuration() - BLINK_TIME) / 22F + 0.454F;
        }
        else
        {
            effectAlpha = Main.getAlpha(time,
                    potion.getMaxTime(),
                    potion.getFadeInTime(),
                    potion.getFadeOutTime());
        }
        return effectAlpha;
    }

    /**
     * Updates several fields relating to what items are being held by the
     * player.
     *
     * @return If true, then there have been changes in the hotbar.
     *
     * @see #drawHotbar(float)
     * @since 0.2-1.16.4-forge
     */
    private static boolean updateHotbar()
    {
        ConfigManager.TimeValues health =
                ConfigManager.getInstance().getHealthTime();
        ConfigManager.TimeValues hunger =
                ConfigManager.getInstance().getHungerTime();
        double handTime = ConfigManager.getInstance().getHandTime();

        Minecraft mc = Minecraft.getInstance();
        Item item, handItem;
        boolean changed = false;

        mainHandLock = offHandLock = false;
        mapLock = 0;
        for (int i = 0, l = Hand.values().length; i < l; i++)
        {
            item =
                    Optional.ofNullable(mc.player.getHeldItem(Hand.values()[i]))
                            .map(ItemStack::getItem)
                            .orElse(null);
            if (item != null)
            {
                if (item instanceof ShootableItem)
                {
                    /*
                     * Enables the crosshairs and lock the hand
                     * whenever the bow is drawn.
                     */
                    if (item instanceof BowItem)
                    {
                        if (mc.player.isHandActive() && mc.player.getActiveHand()
                                .ordinal() == i)
                        {
                            switch (i)
                            {
                            case 0:
                                mainHandLock = true;
                                break;
                            case 1:
                                offHandLock = true;
                                break;
                            }
                        }
                    }
                    /*
                     * Enables the crosshairs and lock the hand
                     * whenever a loaded crossbow is equipped.
                     */
                    else if (item instanceof CrossbowItem)
                    {
                        if (CrossbowItem.isCharged(mc.player.getHeldItem(
                                Hand.values()[i])))
                        {
                            switch (i)
                            {
                            case 0:
                                mainHandLock = true;
                                break;
                            case 1:
                                offHandLock = true;
                                break;
                            }
                        }
                    }
                    /*
                     * We don't know how modded items behave
                     * exactly, so we keep it safe and enable the
                     * crosshairs at all times for any shootable
                     * items.
                     */
                    else
                    {
                        switch (i)
                        {
                        case 0:
                            mainHandLock = true;
                            break;
                        case 1:
                            offHandLock = true;
                            break;
                        }
                    }
                }
                /*
                 * Items that have the crosshairs enabled for as long
                 * as the item is actively being used.
                 */
                else if (item instanceof TridentItem || item instanceof ShieldItem)
                {
                    if (mc.player.isHandActive() && mc.player.getActiveHand()
                            .ordinal() == i)
                    {
                        switch (i)
                        {
                        case 0:
                            mainHandLock = true;
                            break;
                        case 1:
                            offHandLock = true;
                            break;
                        }
                    }
                }
                /*
                 * Items that have the crosshairs enabled for as long
                 * as the item is being held at all.
                 */
                else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem || item instanceof FishingRodItem)
                {
                    switch (i)
                    {
                    case 0:
                        mainHandLock = true;
                        break;
                    case 1:
                        offHandLock = true;
                        break;
                    }
                }
                /*
                 * Maps get special treatment. The hand is locked,
                 * but the crosshairs are not enabled.
                 */
                else if (item instanceof FilledMapItem)
                {
                    mapLock = mapLock | (2 - i);
                }
            }

            /*
             * Checks for a change in the item
             */
            if (i == 0)
            {
                handItem = mainHandItem;
            }
            else
            {
                handItem = offHandItem;
            }
            if (item != handItem)
            {
                changed = true;
                /*
                 * Briefly shows the health and hunger bar whenever
                 * food is switched to.
                 */
                if (item.isFood())
                {
                    healthTime = health.getMaxTime() - (healthTime > 0 ?
                                                        health.getFadeInTime() :
                                                        0);
                    hungerTime = hunger.getMaxTime() - (hungerTime > 0 ?
                                                        hunger.getFadeInTime() :
                                                        0);
                }
                if (i == 0)
                {
                    mainHandItem = item;
                    mainHandTime = handTime;
                }
                else if (i == 1)
                {
                    offHandItem = item;
                    offHandTime = handTime;
                }
            }
        }
        return changed;
    }

    /**
     * Determines whether or not to draw the hotbar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the hotbar.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawHotbar(float ticks)
    {
        ConfigManager.TimeValues hotbar =
                ConfigManager.getInstance().getHotbarTime();
        double handTime = ConfigManager.getInstance().getHandTime();
        Minecraft mc = Minecraft.getInstance();
        int hotbarSlot;

        boolean changed = updateHotbar();
        /*
         * Checks for a change in what slot is used.
         */
        hotbarSlot = mc.player.inventory.currentItem;
        if (selectedHotbarSlot != hotbarSlot)
        {
            selectedHotbarSlot = hotbarSlot;
            mainHandTime = handTime;
            changed = true;
        }

        if (changed)
        {
            hotbarTime = hotbar.getMaxTime() - (hotbarTime > 0 ?
                                                hotbar.getFadeInTime() :
                                                0);
        }
        else if (hotbarTime > 0)
        {
            hotbarTime -= ticks;
        }

        return hotbarTime == 0;
    }

    /**
     * Determines whether or not to draw the health bar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the health.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawHealth(float ticks)
    {
        /*
         * When the health percentage falls to this level or below, the
         * health bar won't disappear, allowing the player to be constantly
         * reminded of their low health.
         */
        final double HEALTH_BOUNDARY =
                ConfigManager.getInstance().getMinHealth();
        ConfigManager.TimeValues healthTimes =
                ConfigManager.getInstance().getHealthTime();
        Minecraft mc = Minecraft.getInstance();
        boolean changed = false;

        /*
         * Checks for a change in current or max health.
         */
        if (health != mc.player.getHealth())
        {
            health = mc.player.getHealth();
            changed = true;
        }
        if (maxHealth != mc.player.getMaxHealth())
        {
            maxHealth = mc.player.getMaxHealth();
            changed = true;
        }
        if (changed)
        {
            healthTime = healthTimes.getMaxTime() - (healthTime > 0 ?
                                                     healthTimes.getFadeInTime() :
                                                     0);
        }
        else if (healthTime > 0)
        {
            healthTime -= ticks;
        }
        /*
         * Only makes a change if the player is healthy. Otherwise,
         * the bar is shown.
         */
        if (health / maxHealth > HEALTH_BOUNDARY && !mc.player.isPotionActive(
                Effects.WITHER) && !mc.player.isPotionActive(Effects.POISON))
        {
            if (healthTime > 0)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translatef(0F,
                        getHealthTranslation(),
                        0F);
                setAlpha(Main.getAlpha(healthTime,
                        healthTimes.getMaxTime(),
                        healthTimes.getFadeInTime(),
                        healthTimes.getFadeOutTime()));
                return false;
            }
            return true;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0F,
                getHealthTranslation(),
                0F);
        return false;
    }

    /**
     * Determines whether or not to draw the hunger bar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the hunger bar.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawHunger(float ticks)
    {
        /*
         * When hunger falls to this level or below, the hunger bar won't
         * disappear, allowing the player to be constantly reminded of their
         * low hunger.
         */
        final int HUNGER_BOUNDARY = ConfigManager.getInstance().getMinHunger();
        ConfigManager.TimeValues hungerTimes =
                ConfigManager.getInstance().getHungerTime();
        Minecraft mc = Minecraft.getInstance();
        boolean changed = false;

        if (hunger != mc.player.getFoodStats().getFoodLevel())
        {
            hunger = mc.player.getFoodStats().getFoodLevel();
            changed = true;
        }
        if (isFoodPoisoned != mc.player.isPotionActive(Effects.HUNGER))
        {
            isFoodPoisoned =
                    mc.player.isPotionActive(Effects.HUNGER);
            changed = true;
        }

        if (changed || hunger <= HUNGER_BOUNDARY)
        {
            hungerTime = hungerTimes.getMaxTime() - (hungerTime > 0 ?
                                                     hungerTimes.getFadeInTime() :
                                                     0);
        }
        else if (hungerTime > 0)
        {
            hungerTime -= ticks;
        }
        /*
         * Only fade a change if the player is satisfied. Otherwise,
         * the bar is shown.
         */
        if (hunger > HUNGER_BOUNDARY && !mc.player.isPotionActive(Effects.HUNGER))
        {
            if (hungerTime > 0)
            {
                setAlpha(Main.getAlpha(hungerTime,
                        hungerTimes.getMaxTime(),
                        hungerTimes.getFadeInTime(),
                        hungerTimes.getFadeOutTime()));
                GlStateManager.pushMatrix();
                GlStateManager.translatef(0F,
                        getHealthTranslation(),
                        0F);
                return false;
            }
            return true;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0F,
                getHealthTranslation(),
                0F);
        return false;
    }

    /**
     * Determines whether or not to draw the armor bar, adjusting the alpha as
     * needed.
     *
     * @param ticks
     *
     * @return If true, then cancel drawing the armor bar.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawArmor(float ticks)
    {
        ConfigManager.TimeValues healthTimes;
        /*
         * Renders armor along with health.
         */
        if (ConfigManager.getInstance().shouldShowArmor() && healthTime > 0)
        {
            healthTimes = ConfigManager.getInstance().getHealthTime();
            setAlpha(Main.getAlpha(healthTime,
                    healthTimes.getMaxTime(),
                    healthTimes.getFadeInTime(),
                    healthTimes.getFadeOutTime()));
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0F,
                    getHealthTranslation(),
                    0F);
            return false;
        }
        return true;
    }

    /**
     * Repositions the oxygen bar.
     *
     * @param ticks
     *
     * @return If true, then cancel drawing the oxygen bar.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawAir(float ticks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0F,
                getHealthTranslation(),
                0F);
        return false;
    }

    /**
     * Determines whether or not to draw the mount's health, adjusting the alpha
     * as needed.
     *
     * @param ticks
     *
     * @return If true, then cancel drawing the mount health bar.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawMountHealth(float ticks)
    {
        /*
         * When the health percentage falls to this level or below, the
         * health bar won't disappear, allowing the player to be constantly
         * reminded of their low health.
         */
        final double HEALTH_BOUNDARY =
                ConfigManager.getInstance().getMinHealth();

        ConfigManager.TimeValues healthTimes =
                ConfigManager.getInstance().getHealthTime();
        Minecraft mc = Minecraft.getInstance();
        Entity tmp = mc.player.getRidingEntity();
        boolean changed = false;

        if (tmp == null || !(tmp instanceof LivingEntity))
        {
            mountHealth = -1;
            mountMaxHealth = -1;
        }
        else
        {
            LivingEntity mount = (LivingEntity) tmp;
            if (mountHealth != mount.getHealth())
            {
                mountHealth = mount.getHealth();
                changed = true;
            }
            if (mountMaxHealth != mount.getMaxHealth())
            {
                mountMaxHealth = mount.getMaxHealth();
                changed = true;
            }
        }

        if (changed)
        {
            mountTime = healthTimes.getMaxTime() - (mountTime > 0 ?
                                                    healthTimes.getFadeInTime() :
                                                    0);
        }
        else if (mountTime > 0)
        {
            mountTime -= ticks;
        }

        /*
         * Only makes a change if the player is healthy. Otherwise,
         * the bar is shown.
         */
        if (mountHealth / mountMaxHealth > HEALTH_BOUNDARY)
        {
            if (mountTime > 0)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translatef(0F,
                        getHealthTranslation(),
                        0F);
                setAlpha(Main.getAlpha(mountTime,
                        healthTimes.getMaxTime(),
                        healthTimes.getFadeInTime(),
                        healthTimes.getFadeOutTime()));
                return false;
            }
            return true;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0F,
                getHealthTranslation(),
                0F);
        return false;
    }

    /**
     * Determines whether or not to draw the horse jump bar, adjusting the alpha
     * as needed.
     *
     * @param ticks
     *
     * @return If true, then cancel drawing the jump bar.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawJumpbar(float ticks)
    {
        ConfigManager.TimeValues jump =
                ConfigManager.getInstance().getJumpTime();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player.getHorseJumpPower() > 0)
        {
            jumpTime = jump.getMaxTime() - (jumpTime > 0 ?
                                            jump.getFadeInTime() :
                                            0);
        }
        else if (jumpTime > 0)
        {
            jumpTime -= ticks;
        }
        return jumpTime == 0;
    }

    /**
     * Determines whether or not to draw the experience bar, adjusting the alpha
     * as needed.
     *
     * @param ticks
     *
     * @return If true, then cancel drawing the experience bar.
     *
     * @since 0.2-1.16.4-forge
     */
    public static boolean drawExperience(float ticks)
    {
        ConfigManager.TimeValues experience =
                ConfigManager.getInstance().getExperenceTime();
        Minecraft mc = Minecraft.getInstance();
        boolean changed = false;

        if (experienceProgress != mc.player.experience)
        {
            experienceProgress = mc.player.experience;
            changed = true;
        }
        if (changed)
        {
            experienceTime = experience.getMaxTime() - (experienceTime > 0 ?
                                                        experience.getFadeInTime() :
                                                        0);
        }
        else if (experienceTime > 0)
        {
            experienceTime -= ticks;
        }
        return experienceProgress == 0;
    }
}
