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

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static markil3.immersive_hud.TimerUtils.resetAlpha;

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
     * Run whenever the player clicks a mouse button. This brings the hands into
     * view, and briefly brings the health and hunger into view if the held item
     * is food.
     *
     * @param event - event data
     */
    @SubscribeEvent
    public static void onClick(final PlayerInteractEvent event)
    {
        try
        {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
            {
                Item item =
                        Optional.ofNullable(event.getItemStack())
                                .map(ItemStack::getItem)
                                .orElse(null);
                TimerUtils.onClick(event.getHand(), item);
            });
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running markil3.immersive_hud.EventBus#onClick " +
                            "event",
                    e);
        }
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
        try
        {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
            {
                TimerUtils.resetMountHealth();
            });
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running markil3.immersive_hud.EventBus#onMount " +
                            "event",
                    e);
        }
    }

    /**
     * Run whenever a hand is rendered in 1st person. This brings the player
     * hands into and out of view as needed.
     *
     * @param event - event data
     */
    @SubscribeEvent
    public static void onRenderHand(final RenderSpecificHandEvent event)
    {
        try
        {
            if (TimerUtils.onRenderHand(event.getHand(),
                    event.getPartialTicks()))
            {
                event.setCanceled(true);
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running markil3.immersive_hud" +
                            ".EventBus#onRenderHand event",
                    e);
        }
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
        try
        {
            Minecraft mc = Minecraft.getInstance();
            boolean fadeIn = false;

            switch (event.getType())
            {
            case CROSSHAIRS:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    if (TimerUtils.drawCrosshair(event.getPartialTicks()))
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
            case POTION_ICONS:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    TimerUtils.updatePotions(mc.player);
                    event.setCanceled(true);
                    RenderUtils.renderPotionIcons(mc,
                            mc.ingameGUI, event.getPartialTicks());
                    resetAlpha();
                }
                break;
            case HOTBAR:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    event.setCanceled(true);
                    if (!TimerUtils.drawHotbar(event.getPartialTicks()))
                    {
                        RenderUtils.renderHotbar(mc, mc.ingameGUI,
                                event.getPartialTicks(), TimerUtils.hotbarTime);
                    }
                }
                break;
            case HEALTH:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    if (TimerUtils.drawHealth(event.getPartialTicks()))
                    {
                        event.setCanceled(true);
                    }
                }
                else if (event instanceof RenderGameOverlayEvent.Post)
                {
                    GlStateManager.popMatrix();
                    resetAlpha();
                }
                break;
            case FOOD:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    if (TimerUtils.drawHunger(event.getPartialTicks()))
                    {
                        event.setCanceled(true);
                    }
                }
                /*
                 * Reset the transparency.
                 */
                else if (event instanceof RenderGameOverlayEvent.Post)
                {
                    GlStateManager.popMatrix();
                    resetAlpha();
                }
                break;
            case ARMOR:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    if (TimerUtils.drawArmor(event.getPartialTicks()))
                    {
                        event.setCanceled(true);
                    }
                }
                /*
                 * Reset the transparency.
                 */
                else if (event instanceof RenderGameOverlayEvent.Post)
                {
                    GlStateManager.popMatrix();
                    resetAlpha();
                }
                break;
            case AIR:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    if (TimerUtils.drawAir(event.getPartialTicks()))
                    {
                        event.setCanceled(true);
                    }
                }
                /*
                 * Reset the transparency.
                 */
                else if (event instanceof RenderGameOverlayEvent.Post)
                {
                    GlStateManager.popMatrix();
                    resetAlpha();
                }
                break;
            case HEALTHMOUNT:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    if (TimerUtils.drawMountHealth(event.getPartialTicks()))
                    {
                        event.setCanceled(true);
                    }
                }
                else if (event instanceof RenderGameOverlayEvent.Post)
                {
                    GlStateManager.popMatrix();
                    resetAlpha();
                }
                break;
            case JUMPBAR:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    event.setCanceled(true);
                    if (!TimerUtils.drawJumpbar(event.getPartialTicks()))
                    {
                        RenderUtils.renderHorseJumpBar(mc,
                                mc.ingameGUI,
                                event.getPartialTicks(),
                                TimerUtils.jumpTime);
                    }
                }
                break;
            case EXPERIENCE:
                if (event instanceof RenderGameOverlayEvent.Pre)
                {
                    event.setCanceled(true);
                    if (!TimerUtils.drawExperience(event.getPartialTicks()))
                    {
                        RenderUtils.renderExperience(mc,
                                mc.ingameGUI,
                                event.getPartialTicks(),
                                TimerUtils.experienceTime);
                    }
                }
                break;
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running markil3.immersive_hud.EventBus#onGUIDraw event",
                    e);
        }
    }
}
