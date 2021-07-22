/**
 * Copyright (C) 2021 Markil 3
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

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

/**
 * Contains the logic for changing the HUD.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-fabric
 */
public class TimerUtils
{
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

    private static HashMap<StatusEffect, Double> effectTime = new HashMap<>();

    /**
     * How many more ticks the hotbar will be onscreen. Every tick is 1/20th of
     * a second.
     */
    private static double hotbarTime;
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
    private static double jumpTime;

    /**
     * How many more ticks the experience bar will be onscreen. Every tick is
     * 1/20th of a second.
     */
    private static double experienceTime;
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
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
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
        return (int) (22F * (1 - Main.getAlpha(hotbarTime,
                hotbar.getMaxTime(),
                hotbar.getFadeInTime(),
                hotbar.getFadeOutTime())));
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
                ConfigManager.getInstance().getExperienceTime();
        return (int) ((12F + (22 - getHotbarTranslation())) * (1 - Main.getAlpha(
                experienceTime,
                experience.getMaxTime(),
                experience.getFadeInTime(),
                experience.getFadeOutTime()))) + getHotbarTranslation();
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
                ConfigManager.getInstance().getExperienceTime();
        return (int) ((12F + (22 - getHotbarTranslation())) * (1 - Main.getAlpha(
                jumpTime,
                jump.getMaxTime(),
                jump.getFadeInTime(),
                jump.getFadeOutTime()))) + getHotbarTranslation();
    }

    /**
     * Obtains the offset that the status bars (health, hunger, etc.) gets from
     * their usual position.
     *
     * @return The offset.
     */
    public static float getHealthTranslation()
    {
        ConfigManager.TimeValues hotbar =
                ConfigManager.getInstance().getHotbarTime();
        ConfigManager.TimeValues experience =
                ConfigManager.getInstance().getExperienceTime();
        ConfigManager.TimeValues jump =
                ConfigManager.getInstance().getJumpTime();
        ConfigManager.TimeValues healthTimes = ConfigManager.getInstance().getHealthTime();
        ConfigManager.TimeValues hungerTimes = ConfigManager.getInstance().getHungerTime();

        float hotbarAlpha = Main.getAlpha(hotbarTime,
                hotbar.getMaxTime(),
                hotbar.getFadeInTime(),
                hotbar.getFadeOutTime());
        float experienceAlpha = Main.getAlpha(experienceTime,
                experience.getMaxTime(),
                experience.getFadeInTime(),
                experience.getFadeOutTime());
        float jumpAlpha = Main.getAlpha(jumpTime,
                jump.getMaxTime(),
                jump.getFadeInTime(),
                jump.getFadeOutTime());
        float statusAlpha = Main.getAlpha(healthTime,
                healthTimes.getMaxTime(),
                healthTimes.getFadeInTime(),
                healthTimes.getFadeOutTime());
        statusAlpha = Math.max(statusAlpha, Main.getAlpha(hungerTime,
                hungerTimes.getMaxTime(),
                hungerTimes.getFadeInTime(),
                hungerTimes.getFadeOutTime()));
        /*
         * Draw it in its standard location until it is completely transparent, then move it
         * instantly.
         */
        statusAlpha = (float) Math.ceil(statusAlpha);
        return 7 * (1 - Math.max(experienceAlpha,
                jumpAlpha)) + 22 * (1 - hotbarAlpha) + 42 * (1 - statusAlpha);
    }

    /**
     * Run whenever the player clicks a mouse button. This brings the hands into
     * view, and briefly brings the health and hunger into view if the held item
     * is food.
     *
     * @param hand - The hand being triggered
     * @param clear - Whether the hand it being enabled or disabled.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static void onClick(Hand hand, boolean clear)
    {
        ConfigManager.TimeValues health =
                ConfigManager.getInstance().getHealthTime();
        ConfigManager.TimeValues hunger =
                ConfigManager.getInstance().getHungerTime();
        double handTime = ConfigManager.getInstance().getHandTime();

        MinecraftClient mc = MinecraftClient.getInstance();
        LivingEntity entity;
        Item item;
        Iterator<ItemStack> hands;
        if (mc.getCameraEntity() instanceof LivingEntity)
        {
            entity = (LivingEntity) mc.getCameraEntity();
        }
        else
        {
            return;
        }
        if (clear)
        {
            item = null;
        }
        else
        {
            hands = entity.getItemsHand().iterator();
            if (hand == Hand.OFF_HAND)
            {
                hands.next();
            }
            item =
                    Optional.ofNullable(hands.next())
                            .filter(stack -> !stack.isEmpty())
                            .map(ItemStack::getItem)
                            .orElse(null);
        }
        crosshairTime = ConfigManager.getInstance().getCrosshairTime();
        if (hand == Hand.MAIN_HAND)
        {
            mainHandTime = handTime;
            mainHandLock = item != null;
        }
        else
        {
            offHandTime = handTime;
            offHandLock = item != null;
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
        else
        {
            System.out.println("Unusing hand " + hand);
        }
    }

    /**
     * Run whenever a hand is rendered in 1st person. This brings the player
     * hands into and out of view as needed.
     *
     * @param hand - The hand being rendered.
     * @param matrixStack - The rendering stack.
     *
     * @return If true, then don't render this hand at all.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static boolean onRenderHand(Hand hand,
                                       MatrixStack matrixStack,
                                       float ticks)
    {
        final float HAND_UP_TIME = 20F;

        boolean hideHands = ConfigManager.getInstance().hideHands();
        if (hideHands)
        {
            switch (hand)
            {
            case OFF_HAND:
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
                        matrixStack
                                .translate(0,
                                        -1.0F * (HAND_UP_TIME - offHandTime) /
                                                HAND_UP_TIME,
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
                        matrixStack
                                .translate(0,
                                        -1.0F * (HAND_UP_TIME - mainHandTime) /
                                                HAND_UP_TIME,
                                        0);
                    }
                }
                break;
            }
        }
        return false;
    }

    /**
     * Determines whether or not to draw the crosshair, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the crosshair.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static boolean drawCrosshair(float ticks)
    {
        final double CROSSHAIR_TIME =
                ConfigManager.getInstance().getCrosshairTime();

        MinecraftClient mc = MinecraftClient.getInstance();
        Entity entity = mc.getCameraEntity();
        boolean changed = false;
        boolean canceled = false;

        boolean hideCrosshair = ConfigManager.getInstance().hideCrosshair();
        if (hideCrosshair)
        {
            if (mc.crosshairTarget != null && mc.crosshairTarget
                    .getType() != HitResult.Type.MISS)
            {
                if (mc.crosshairTarget.getType() == HitResult.Type
                        .ENTITY)
                {
                    if (((EntityHitResult) mc.crosshairTarget).getEntity() != entity
                            .getVehicle())
                    {
                        changed = true;
                    }
                }
                else
                {
                    changed = true;
                }
            }
            if (changed || mainHandLock || offHandLock || crosshairTime
                    > 0)
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
        }
        return canceled;
    }

    /**
     * Determines whether or not to draw the mount's health, adjusting the alpha
     * as needed.
     *
     * @return If true, then cancel drawing the mount health bar.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static boolean drawMountHealth(MatrixStack stack, float ticks)
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

        MinecraftClient mc = MinecraftClient.getInstance();
        Entity entity = mc.getCameraEntity();
        Entity tmp = entity.getVehicle();
        LivingEntity mount;
        boolean changed = false;

        if (tmp == null || !(tmp instanceof LivingEntity))
        {
            mountHealth = -1;
            mountMaxHealth = -1;
        }
        else
        {
            mount = (LivingEntity) tmp;
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
                stack.push();
                stack.translate(0F,
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
        stack.push();
        stack.translate(0F,
                getHealthTranslation(),
                0F);
        return false;
    }

    /**
     * Updates the timings of some of the potions currently affecting the
     * player.
     *
     * @param player - The player being affected.
     */
    public static void updatePotions(ClientPlayerEntity player)
    {
        for (StatusEffect effect :
                new HashSet<>(effectTime.keySet()))
        {
            if (!player.hasStatusEffect(effect))
            {
                effectTime.remove(effect);
            }
        }
    }

    /**
     * Updates the timings of some of the potions currently affecting the
     * player.
     *
     * @param effectinstance - The effect to update
     * @param ticks
     */
    public static boolean updatePotion(StatusEffectInstance effectinstance,
                                       float ticks)
    {
        final int BLINK_TIME = 200;

        ConfigManager.TimeValues potion =
                ConfigManager.getInstance().getPotionTime();

        Double time = effectTime.get(effectinstance.getEffectType());
        if (time == null)
        {
            effectTime.put(effectinstance.getEffectType(),
                    (time = (double) potion.getMaxTime()));
        }
        else
        {
            effectTime.put(effectinstance.getEffectType(), (time -= ticks));
        }
        return Main.getAlpha(time,
                potion.getMaxTime(),
                potion.getFadeInTime(),
                potion.getFadeOutTime()) > 0 || effectinstance.getDuration() <= BLINK_TIME;
    }

    /**
     * Obtains how transparent
     *
     * @param effectinstance
     *
     * @return
     */
    public static float getPotionAlpha(StatusEffectInstance effectinstance)
    {
        ConfigManager.TimeValues potion =
                ConfigManager.getInstance().getPotionTime();
        final int BLINK_TIME = 200;
        float effectAlpha = 0.0F;
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
            effectAlpha =
                    Main.getAlpha(effectTime.getOrDefault(effectinstance.getEffectType(),
                            0.0),
                            potion.getMaxTime(),
                            potion.getFadeInTime(),
                            potion.getFadeOutTime());
        }
        return effectAlpha;
    }

    /**
     * Determines whether or not to draw the armor bar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the armor bar.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static boolean drawArmor()
    {
        ConfigManager.TimeValues healthTimes;
        /*
         * Sets the armor to fade in and out in sync with the health.
         * Technically, since this method gets called before health, it will
         * be 1 tick behind, but I'm not too concerned about it.
         */
        if (ConfigManager.getInstance().shouldShowArmor() && healthTime > 0)
        {
            healthTimes = ConfigManager.getInstance().getHealthTime();
            setAlpha(Main.getAlpha(healthTime,
                    healthTimes.getMaxTime(),
                    healthTimes.getFadeInTime(),
                    healthTimes.getFadeOutTime()));
            return false;
        }
        return true;
    }

    /**
     * Determines whether or not to draw the health bar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the health.
     *
     * @since 0.1-1.16.4-fabric
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

        MinecraftClient mc = MinecraftClient.getInstance();
        LivingEntity entity;
        if (mc.getCameraEntity() instanceof LivingEntity)
        {
            entity = (LivingEntity) mc.getCameraEntity();
        }
        else
        {
            return true;
        }
        boolean changed = false;
        boolean canceled;

        /**
         * Checks for a change in current or max health.
         */
        if (health != entity.getHealth())
        {
            health = entity.getHealth();
            changed = true;
        }
        if (maxHealth != entity.getMaxHealth())
        {
            maxHealth = entity.getMaxHealth();
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
        if (health / maxHealth > HEALTH_BOUNDARY && !entity
                .hasStatusEffect(StatusEffects.WITHER) && !entity.hasStatusEffect(
                StatusEffects.POISON))
        {
            setAlpha(Main.getAlpha(healthTime,
                    healthTimes.getMaxTime(),
                    healthTimes.getFadeInTime(),
                    healthTimes.getFadeOutTime()));
        }
        canceled =
                healthTime == 0 && health / maxHealth > HEALTH_BOUNDARY && !entity
                        .hasStatusEffect(StatusEffects.WITHER) && !entity.hasStatusEffect(
                        StatusEffects.POISON);
        return canceled;
    }

    /**
     * Determines whether or not to draw the hunger bar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the hunger bar.
     *
     * @since 0.1-1.16.4-fabric
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

        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity entity;
        boolean canceled;
        boolean changed = false;

        if (mc.getCameraEntity() instanceof PlayerEntity)
        {
            entity = (PlayerEntity) mc.getCameraEntity();
        }
        else
        {
            return true;
        }

        if (hunger != entity.getHungerManager().getFoodLevel())
        {
            hunger = entity.getHungerManager().getFoodLevel();
            changed = true;
        }
        if (isFoodPoisoned != entity.hasStatusEffect(StatusEffects
                .HUNGER))
        {
            isFoodPoisoned =
                    entity.hasStatusEffect(StatusEffects.HUNGER);
            changed = true;
        }
        if (hunger <= HUNGER_BOUNDARY)
        {
            changed = true;
        }
        if (changed)
        {
            hungerTime = hungerTimes.getMaxTime() - (hungerTime > 0 ?
                                                     hungerTimes.getFadeInTime() :
                                                     0);
        }
        else if (hungerTime > 0)
        {
            hungerTime -= ticks;
        }

        if (hungerTime <= 0 && hunger > HUNGER_BOUNDARY)
        {
            return true;
        }
        /*
         * Only makes a change if the player is healthy. Otherwise,
         * the bar is shown.
         */
        if (hunger > HUNGER_BOUNDARY && !entity.hasStatusEffect(StatusEffects.HUNGER))
        {
            setAlpha(Main.getAlpha(hungerTime,
                    hungerTimes.getMaxTime(),
                    hungerTimes.getFadeInTime(),
                    hungerTimes.getFadeOutTime()));
        }
        return hungerTime == 0 && hunger > HUNGER_BOUNDARY && !entity
                .hasStatusEffect(StatusEffects.HUNGER);
    }

    /**
     * Determines whether or not to draw the horse jump bar, adjusting the alpha
     * as needed.
     *
     * @param stack
     *
     * @return If true, then cancel drawing the jump bar.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static boolean drawJumpbar(MatrixStack stack, float ticks)
    {
        ConfigManager.TimeValues jump =
                ConfigManager.getInstance().getJumpTime();

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity entity;
        if (mc.getCameraEntity() instanceof ClientPlayerEntity)
        {
            entity = (ClientPlayerEntity) mc.getCameraEntity();
        }
        else
        {
            return true;
        }

        if (entity.method_3151() > 0)
        {
            jumpTime = jump.getMaxTime() - (jumpTime > 0 ?
                                            jump.getFadeInTime() :
                                            0);
        }
        else if (jumpTime > 0)
        {
            jumpTime -= ticks;
        }
        if (jumpTime > 0)
        {
            stack.push();
            stack.translate(0F, TimerUtils.getJumpTranslation(), 0F);
            setAlpha(Main.getAlpha(jumpTime,
                    jump.getMaxTime(),
                    jump.getFadeInTime(),
                    jump.getFadeOutTime()));
            return false;
        }
        return true;
    }

    /**
     * Determines whether or not to draw the experience bar, adjusting the alpha
     * as needed.
     *
     * @return If true, then cancel drawing the experience bar.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static boolean drawExperience(MatrixStack stack, float ticks)
    {
        ConfigManager.TimeValues experience =
                ConfigManager.getInstance().getExperienceTime();

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity entity;
        boolean changed = false;

        if (mc.getCameraEntity() instanceof ClientPlayerEntity)
        {
            entity = (ClientPlayerEntity) mc.getCameraEntity();
        }
        else
        {
            return true;
        }

        if (experienceProgress != entity.totalExperience)
        {
            experienceProgress = entity.totalExperience;
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
        if (experienceTime > 0)
        {
            stack.push();
            stack.translate(0F, TimerUtils.getExperienceTranslation(), 0F);
            setAlpha(Main.getAlpha(experienceTime,
                    experience.getMaxTime(),
                    experience.getFadeInTime(),
                    experience.getFadeOutTime()));
            return false;
        }
        return true;
    }

    /**
     * Determines whether or not to draw the hotbar. Note that this method does
     * not directly change the transparency of the hotbar, only checking whether
     * or not to draw it.
     *
     * @return If true, then cancel drawing the hotbar.
     *
     * @see #recolorHotbar(MatrixStack)
     * @since 0.1-1.16.4-fabric
     */
    public static boolean updateHotbar(float ticks)
    {
        ConfigManager.TimeValues health =
                ConfigManager.getInstance().getHealthTime();
        ConfigManager.TimeValues hunger =
                ConfigManager.getInstance().getHungerTime();
        ConfigManager.TimeValues hotbar =
                ConfigManager.getInstance().getHotbarTime();
        double handTime = ConfigManager.getInstance().getHandTime();

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity entity;
        Item item, handItem = null;
        boolean changed = false;

        if (mc.getCameraEntity() instanceof ClientPlayerEntity)
        {
            entity = (ClientPlayerEntity) mc.getCameraEntity();
        }
        else
        {
            return true;
        }

        mainHandLock = offHandLock = false;
        mapLock = 0;
        int i = 0;
        for (ItemStack stack : entity.getItemsHand())
        {
            item =
                    Optional.ofNullable(stack)
                            .map(ItemStack::getItem)
                            .orElse(null);
            if (item != null)
            {
                if (item instanceof RangedWeaponItem)
                {
                    /*
                     * Enables the crosshairs and lock the hand
                     * whenever the bow is drawn.
                     */
                    if (item instanceof BowItem)
                    {
                        if (entity.isUsingItem() && entity
                                .getActiveHand()
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
                        if (CrossbowItem.isCharged(stack))
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
                /**
                 * Items that have the crosshairs enabled for as long
                 * as the item is actively being used.
                 */
                else if (item instanceof TridentItem || item
                        instanceof ShieldItem)
                {
                    if (entity.isUsingItem() && entity
                            .getActiveHand() != null && entity.getActiveHand()
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
                /**
                 * Items that have the crosshairs enabled for as long
                 * as the item is being held at all.
                 */
                else if (item instanceof ThrowablePotionItem ||
                        item instanceof SnowballItem || item instanceof
                        EggItem || item instanceof EnderPearlItem || item
                        instanceof FishingRodItem)
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
                /**
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
            i++;
        }

        /*
         * Checks for a change in what slot is used.
         */
        if (selectedHotbarSlot != entity.inventory.selectedSlot)
        {
            selectedHotbarSlot = entity.inventory.selectedSlot;
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
     * Determines whether or not to draw the crosshair, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the crosshair.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static void recolorHotbar(MatrixStack stack)
    {
        ConfigManager.TimeValues hotbar =
                ConfigManager.getInstance().getHotbarTime();
        RenderSystem.pushMatrix();
        stack.push();
        RenderSystem.translatef(0F, TimerUtils.getHotbarTranslation(), 0F);
        setAlpha(Main.getAlpha(hotbarTime,
                hotbar.getMaxTime(),
                hotbar.getFadeInTime(),
                hotbar.getFadeOutTime()));
    }
}
