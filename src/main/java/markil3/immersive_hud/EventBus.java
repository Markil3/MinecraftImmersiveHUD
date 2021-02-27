/*
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

import net.minecraft.client.Minecraft;
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
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Contains the logic for changing the HUD.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-forge
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class EventBus
{
    /**
     * Class logger
     */
    private static final Logger LOGGER = LogManager.getLogger();

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
    private static void setAlpha(float alpha)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
    }

    /**
     * Resets the transparency level.
     *
     * @see #setAlpha(float)
     */
    private static void resetAlpha()
    {
        setAlpha(1.0F);
    }

    /**
     * Run whenever the player clicks a mouse button. This brings the hands into
     * view, and briefly brings the health and hunger into view if the held item
     * is food.
     *
     * @param event - event data
     */
    @SubscribeEvent
    public static void onClick(final PlayerInteractEvent event)
    {
        DistExecutor.safeRunWhenOn(Dist.CLIENT,
                (DistExecutor.SafeSupplier<DistExecutor.SafeRunnable>) () -> new DistExecutor.SafeRunnable()
                {
                    @Override
                    public void run()
                    {
                        Item item =
                                Optional.ofNullable(event.getItemStack())
                                        .map(ItemStack::getItem)
                                        .orElse(null);
                        crosshairTime = VISUAL_TIME;
                        if (event.getHand() == Hand.MAIN_HAND)
                        {
                            mainHandTime = HAND_TIME;
                        }
                        else
                        {
                            offHandTime = HAND_TIME;
                        }
                        if (item != null)
                        {
                            if (item.isFood())
                            {
                                healthTime = HEALTH_TIME;
                                hungerTime = HEALTH_TIME;
                            }
                        }
                    }
                });
    }

    /**
     * Run whenever the player mounts something. This brings the mount's health
     * bar into view.
     * <p>
     * While this event is called on both client and server, only on the client
     * will anything happen.
     *
     * @param event - event data
     */
    @SubscribeEvent
    public static void onMount(final EntityMountEvent event)
    {
        DistExecutor.safeRunWhenOn(Dist.CLIENT,
                (DistExecutor.SafeSupplier<DistExecutor.SafeRunnable>) () -> new DistExecutor.SafeRunnable()
                {
                    @Override
                    public void run()
                    {
                        mountTime = VISUAL_TIME;
                    }
                });
    }

    /**
     * Run whenever a hand is rendered in 1st person. This brings the player
     * hands into and out of view as needed.
     *
     * @param event - event data
     */
    @SubscribeEvent
    public static void onRenderHand(final RenderHandEvent event)
    {
        final int HAND_UP_TIME = 20;
        switch (event.getHand())
        {
        case OFF_HAND:
            if (mainHandTime > 0 && mainHandTime < HAND_UP_TIME)
            {
                /*
                 * Undo the transformation of the previous hand
                 */
                event.getMatrixStack()
                        .translate(0,
                                1.0F * (HAND_UP_TIME - mainHandTime) / HAND_UP_TIME,
                                0);
            }
            if (offHandTime == 0)
            {
                event.setCanceled(true);
            }
            else if (!offHandLock && (mapLock & 0b01) == 0)
            {
                offHandTime--;
                if (offHandTime < HAND_UP_TIME)
                {
                    event.getMatrixStack()
                            .translate(0,
                                    -1.0F * (HAND_UP_TIME - offHandTime) / HAND_UP_TIME,
                                    0);
                }
            }
            break;
        case MAIN_HAND:
            if (mainHandTime == 0)
            {
                event.setCanceled(true);
            }
            else if (!mainHandLock && (mapLock & 0b10) == 0)
            {
                mainHandTime--;
                if (mainHandTime < HAND_UP_TIME)
                {
                    event.getMatrixStack()
                            .translate(0,
                                    -1.0F * (HAND_UP_TIME - mainHandTime) / HAND_UP_TIME,
                                    0);
                }
            }
            break;
        }
    }

    /**
     * Determines whether or not to draw the crosshair, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the crosshair.
     *
     * @since 0.2-1.16.4-forge
     */
    private static boolean drawCrosshair()
    {
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
     * Updates several fields relating to what items are being held by the
     * player.
     *
     * @return If true, then there have been changes in the hotbar.
     *
     * @see #drawHotbar()
     * @since 0.2-1.16.4-forge
     */
    private static boolean updateHotbar()
    {
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
                else if (item instanceof ThrowablePotionItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem || item instanceof FishingRodItem)
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
    private static boolean drawHotbar()
    {
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
     * Determines whether or not to draw the health bar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the health.
     *
     * @since 0.2-1.16.4-forge
     */
    private static boolean drawHealth()
    {
        /*
         * When the health percentage falls to this level or below, the
         * health bar won't disappear, allowing the player to be constantly
         * reminded of their low health.
         */
        final float HEALTH_BOUNDARY = 0.5F;
        Minecraft mc = Minecraft.getInstance();
        boolean changed = false;
        /*
         * Skip healthy bars that haven't updated in awhile.
         */
        boolean canceled =
                healthTime == 0 && health / maxHealth > HEALTH_BOUNDARY;

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
     * @since 0.2-1.16.4-forge
     */
    private static boolean drawHunger()
    {
        /*
         * When hunger falls to this level or below, the hunger bar won't
         * disappear, allowing the player to be constantly reminded of their
         * low hunger.
         */
        final int HUNGER_BOUNDARY = 15;
        Minecraft mc = Minecraft.getInstance();
        boolean canceled = hungerTime == 0 && hunger > HUNGER_BOUNDARY;
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
        if (hunger <= HUNGER_BOUNDARY)
        {
            changed = true;
        }
        if (changed)
        {
            hungerTime = HEALTH_TIME;
            setAlpha(Main.getAlpha(hungerTime));
        }
        else if (hungerTime > 0)
        {
            hungerTime--;
        }
        /*
         * Only makes a change if the player is satisfied. Otherwise,
         * the bar is shown.
         */
        return canceled;
    }

    /**
     * Determines whether or not to draw the armor bar, adjusting the alpha as
     * needed.
     *
     * @return If true, then cancel drawing the armor bar.
     *
     * @since 0.2-1.16.4-forge
     */
    private static boolean drawArmor()
    {
        /*
         * Renders armor along with health.
         */
        if (healthTime > 0)
        {
            setAlpha(Main.getAlpha(healthTime));
            return false;
        }
        return true;
    }

    /**
     * Determines whether or not to draw the mount's health, adjusting the alpha
     * as needed.
     *
     * @return If true, then cancel drawing the mount health bar.
     *
     * @since 0.2-1.16.4-forge
     */
    private static boolean drawMountHealth()
    {
        Minecraft mc = Minecraft.getInstance();
        Entity tmp = mc.player.getRidingEntity();
        boolean changed = false;
        if (tmp == null)
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
     * Determines whether or not to draw the horse jump bar, adjusting the alpha
     * as needed.
     *
     * @return If true, then cancel drawing the jump bar.
     *
     * @since 0.2-1.16.4-forge
     */
    private static boolean drawJumpbar()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player.getHorseJumpPower() > 0)
        {
            jumpTime = VISUAL_TIME;
        }
        else if (jumpTime > 0)
        {
            jumpTime--;
        }
        return jumpTime == 0;
    }

    /**
     * Determines whether or not to draw the experience bar, adjusting the alpha
     * as needed.
     *
     * @return If true, then cancel drawing the experience bar.
     *
     * @since 0.2-1.16.4-forge
     */
    private static boolean drawExperience()
    {
        Minecraft mc = Minecraft.getInstance();
        boolean changed = false;

        if (experienceProgress != mc.player.experience)
        {
            experienceProgress = mc.player.experience;
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
        return experienceProgress == 0;
    }

    /**
     * Called whenever we are drawing a part of the GUI. This is where most of
     * the change checks are made and adjustments or overrides are done.
     *
     * @param event - event data
     */
    @SubscribeEvent
    public static void onGUIDraw(final RenderGameOverlayEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        switch (event.getType())
        {
        case CROSSHAIRS:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                if (drawCrosshair())
                {
                    event.setCanceled(true);
                }
            }
            /*
             * Reset the transparency.
             */
            else if (event instanceof RenderGameOverlayEvent.Post)
            {
                resetAlpha();
            }
            break;
        case HOTBAR:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                event.setCanceled(true);
                if (!drawHotbar())
                {
                    RenderUtils.renderHotbar(mc, mc.ingameGUI,
                            event.getMatrixStack(),
                            event.getPartialTicks(), hotbarTime);
                }
            }
            break;
        case HEALTH:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                if (drawHealth())
                {
                    event.setCanceled(true);
                }
            }
            else if (event instanceof RenderGameOverlayEvent.Post)
            {
                resetAlpha();
            }
            break;
        case FOOD:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                if (drawHunger())
                {
                    event.setCanceled(true);
                }
            }
            /*
             * Reset the transparency.
             */
            else if (event instanceof RenderGameOverlayEvent.Post)
            {
                resetAlpha();
            }
            break;
        case ARMOR:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                if (drawArmor())
                {
                    event.setCanceled(true);
                }
            }
            /*
             * Reset the transparency.
             */
            else if (event instanceof RenderGameOverlayEvent.Post)
            {
                resetAlpha();
            }
            break;
        case HEALTHMOUNT:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                if (drawMountHealth())
                {
                    event.setCanceled(true);
                }
            }
            else if (event instanceof RenderGameOverlayEvent.Post)
            {
                resetAlpha();
            }
            break;
        case JUMPBAR:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                event.setCanceled(true);
                if (!drawJumpbar())
                {
                    RenderUtils.renderHorseJumpBar(mc, mc.ingameGUI,
                            event.getMatrixStack(), jumpTime);
                }
            }
            break;
        case EXPERIENCE:
            if (event instanceof RenderGameOverlayEvent.Pre)
            {
                event.setCanceled(true);
                if (!drawExperience())
                {
                    RenderUtils.renderExperience(mc, mc.ingameGUI,
                            event.getMatrixStack(), experienceTime);
                }
            }
            break;
        }
    }
}
