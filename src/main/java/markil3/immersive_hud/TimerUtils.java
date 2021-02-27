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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     * The number of ticks that the hand will be onscreen. Every tick is 1/20th
     * of a second.
     */
    static final int HAND_TIME = 10 * 20;
    /**
     * The number of ticks most elements will be onscreen. Every tick is 1/20th
     * of a second.
     */
    static final int VISUAL_TIME = 6 * 20;
    /**
     * The number of ticks that the health and hunger bars will be onscreen.
     * Every tick is 1/20th of a second.
     */
    static final int HEALTH_TIME = (int) ((float) VISUAL_TIME * 2.5);

    /**
     * How many more ticks the hand will be onscreen. Every tick is 1/20th of a
     * second.
     */
    private static int mainHandTime, offHandTime;

    /**
     * How many more ticks the crosshair will be onscreen. Every tick is 1/20th
     * of a second.
     */
    private static int crosshairTime;
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

    /**
     * How many more ticks the hotbar will be onscreen. Every tick is 1/20th of
     * a second.
     */
    private static int hotbarTime;
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
    private static int healthTime;
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
    private static int hungerTime;
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
    private static int mountTime;
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
    private static int jumpTime;

    /**
     * How many more ticks the experience bar will be onscreen. Every tick is
     * 1/20th of a second.
     */
    private static int experienceTime;
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
        crosshairTime = VISUAL_TIME;
        if (hand == Hand.MAIN_HAND)
        {
            mainHandTime = HAND_TIME;
            mainHandLock = item != null;
        }
        else
        {
            offHandTime = HAND_TIME;
            offHandLock = item != null;
        }
        if (item != null)
        {
            if (item.isFood())
            {
                healthTime = HEALTH_TIME;
                hungerTime = HEALTH_TIME;
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
    public static boolean onRenderHand(Hand hand, MatrixStack matrixStack)
    {
        final int HAND_UP_TIME = 20;
        switch (hand)
        {
        case OFF_HAND:
            if (offHandTime == 0 && !offHandLock)
            {
                return true;
            }
            else if (!offHandLock && (mapLock & 0b01) == 0)
            {
                offHandTime--;
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
                System.out.println("Decreasing main hand time");
                mainHandTime--;
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
    public static boolean drawCrosshair()
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity entity = mc.getCameraEntity();
        boolean changed = false;
        boolean canceled = false;

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
                                   VISUAL_TIME));
        }
        else
        {
            canceled = true;
        }
        if (crosshairTime > 0)
        {
            crosshairTime--;
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
    public static boolean drawMountHealth()
    {
        /*
         * When the health percentage falls to this level or below, the
         * health bar won't disappear, allowing the player to be constantly
         * reminded of their low health.
         */
        final float CONST_BOUNDARY = 0.5F;

        MinecraftClient mc = MinecraftClient.getInstance();
        Entity entity = mc.getCameraEntity();
        Entity tmp = entity.getVehicle();
        LivingEntity mount;
        boolean changed = false;

        if (tmp == null)
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
            mountTime = VISUAL_TIME;
        }
        else if (mountTime > 0)
        {
            mountTime--;
        }

        if (mountTime > 0)
        {
            setAlpha(Main.getAlpha(mountTime));
            return false;
        }
        return true;
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
        /*
         * Sets the armor to fade in and out in sync with the health.
         * Technically, since this method gets called before health, it will
         * be 1 tick behind, but I'm not too concerned about it.
         */
        if (healthTime > 0)
        {
            setAlpha(Main.getAlpha(healthTime));
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
    public static boolean drawHealth()
    {
        /*
         * When the health percentage falls to this level or below, the
         * health bar won't disappear, allowing the player to be constantly
         * reminded of their low health.
         */
        final float HEALTH_BOUNDARY = 0.5F;

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
        boolean canceled =
                healthTime == 0 && health / maxHealth > HEALTH_BOUNDARY;

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
            healthTime = HEALTH_TIME;
        }
        else if (healthTime > 0)
        {
            healthTime--;
        }
        /*
         * Only makes a change if the player is healthy. Otherwise,
         * the bar is shown.
         */
        if (health / maxHealth > HEALTH_BOUNDARY)
        {
            setAlpha(Main.getAlpha(healthTime));
        }
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
    public static boolean drawHunger()
    {
        /*
         * When hunger falls to this level or below, the hunger bar won't
         * disappear, allowing the player to be constantly reminded of their
         * low hunger.
         */
        final int HUNGER_BOUNDARY = 15;

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
            hungerTime = HEALTH_TIME;
        }
        else if (hungerTime > 0)
        {
            hungerTime--;
        }

        canceled = hungerTime == 0 && hunger > HUNGER_BOUNDARY;
        if (!canceled)
        {
            setAlpha(Main.getAlpha(hungerTime));
        }
        return canceled;
    }

    /**
     * Determines whether or not to draw the horse jump bar, adjusting the alpha
     * as needed.
     *
     * @return If true, then cancel drawing the jump bar.
     *
     * @since 0.1-1.16.4-fabric
     */
    public static boolean drawJumpbar()
    {
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
            jumpTime = VISUAL_TIME;
        }
        else if (jumpTime > 0)
        {
            jumpTime--;
        }
        if (jumpTime > 0)
        {
            setAlpha(Main.getAlpha(jumpTime));
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
    public static boolean drawExperience()
    {
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
            experienceTime = VISUAL_TIME;
        }
        else if (experienceTime > 0)
        {
            experienceTime--;
        }
        if (experienceTime > 0)
        {
            setAlpha(Main.getAlpha(experienceTime));
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
     * @see #recolorHotbar()
     * @since 0.1-1.16.4-fabric
     */
    public static boolean updateHotbar()
    {
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
                    healthTime = HEALTH_TIME;
                    hungerTime = HEALTH_TIME;
                }
                if (i == 0)
                {
                    mainHandItem = item;
                    mainHandTime = HAND_TIME;
                }
                else if (i == 1)
                {
                    offHandItem = item;
                    offHandTime = HAND_TIME;
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
            mainHandTime = HAND_TIME;
            changed = true;
        }

        if (changed)
        {
            hotbarTime = VISUAL_TIME;
        }
        else if (hotbarTime > 0)
        {
            hotbarTime--;
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
    public static void recolorHotbar()
    {
        setAlpha(Main.getAlpha(hotbarTime));
    }
}
