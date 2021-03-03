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
package markil3.immersive_hud.mixin;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import markil3.immersive_hud.TimerUtils;

/**
 * This mixin hooks into the HUD class to modify most of the HUD elements. Note
 * that most of the logic can be found in {@link TimerUtils}.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-fabric
 */
@Mixin(InGameHud.class)
public class HudHook
{
    private static final Logger logger = LogManager.getLogger(HudHook.class);

    private StatusEffectInstance currentEffect;

    /**
     * Determines whether or not to draw the crosshair.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"),
            cancellable = true)
    public void startCrosshair(CallbackInfo callbackInfo)
    {
        logger.debug("Starting crosshair");
        if (TimerUtils.drawCrosshair(MinecraftClient.getInstance()
                .getTickDelta()))
        {
            callbackInfo.cancel();
        }
    }

    /**
     * Resets any changes from drawing the crosshair.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderCrosshair", at = @At(value = "TAIL"))
    public void finishCrosshair(CallbackInfo callbackInfo)
    {
        logger.debug("Finishing crosshair");
        /*
         * Resets the color so that nothing else is bothered.
         */
        TimerUtils.resetAlpha();
    }

    /**
     * Determines whether or not to draw the horse jump bar.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountJumpBar", at = @At(value = "HEAD"),
            cancellable = true)
    public void startJumpbar(int x, CallbackInfo callbackInfo)
    {
        logger.debug("Starting jumpbar");
        if (TimerUtils.drawJumpbar(MinecraftClient.getInstance().getTickDelta()))
        {
            callbackInfo.cancel();
        }
    }

    /**
     * Resets any changes from drawing the horse jump bar.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountJumpBar", at = @At(value = "TAIL"))
    public void finishJumpbar(
            int x,
            CallbackInfo callbackInfo)
    {
        logger.debug("Finishing jumpbar");
        GlStateManager.popMatrix();
        /*
         * Resets the color so that nothing else is bothered.
         */
        TimerUtils.resetAlpha();
    }

    /**
     * Determines whether or not to draw the experience bar.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderExperienceBar", at = @At(value = "HEAD"),
            cancellable = true)
    public void startExperience(int x, CallbackInfo callbackInfo)
    {
        logger.debug("Starting experience bar");
        if (TimerUtils.drawExperience(MinecraftClient.getInstance().getTickDelta()))
        {
            callbackInfo.cancel();
        }
    }

    /**
     * Resets any changes from drawing the experience bar.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderExperienceBar", at = @At(value = "TAIL"))
    public void finishExperience(int x, CallbackInfo callbackInfo)
    {
        logger.debug("Finishing experience bar");
        GlStateManager.popMatrix();
        /*
         * Resets the color so that nothing else is bothered.
         */
        TimerUtils.resetAlpha();
    }

    /**
     * Changes the position of the status bars as the ones below them move up
     * and down.
     *
     *
     * @return The same matrix stack, with a new matrix pushed onto it.
     */
    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    public void startStatus(CallbackInfo info)
    {
        logger.debug("Starting status bars");
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0F, TimerUtils.getHealthTranslation(), 0F);
    }

    /**
     * Adjusts the armor GUI.
     *
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars()V", at = @At(value = "INVOKE",
            target = "Lnet" +
                    "/minecraft/util/profiler/Profiler;push" +
                    "(Ljava/lang/String;)V"))
    public void renderArmor(CallbackInfo callbackInfo)
    {
        logger.debug("Rendering armor");
        if (TimerUtils.drawArmor())
        {
            TimerUtils.setAlpha(0F);
        }
    }

    /**
     * Adjusts the health GUI.
     *
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars()V", at = @At(value = "INVOKE",
            target = "Lnet" +
                    "/minecraft/util/profiler/Profiler;swap" +
                    "(Ljava/lang/String;)V",
            ordinal = 0))
    public void renderHealth(CallbackInfo callbackInfo)
    {
        logger.debug("Rendering health");
        /*
         * Resets the transparency from the previous call.
         */
        TimerUtils.setAlpha(1F);
        if (TimerUtils.drawHealth(MinecraftClient.getInstance().getTickDelta()))
        {
            TimerUtils.setAlpha(0F);
        }
    }

    /**
     * Adjusts the food GUI.
     *
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars()V", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 1))
    public void renderFood(CallbackInfo callbackInfo)
    {
        logger.debug("Rendering food");
        /*
         * Resets the transparency from the previous call.
         */
        TimerUtils.setAlpha(1F);
        if (TimerUtils.drawHunger(MinecraftClient.getInstance().getTickDelta()))
        {
            TimerUtils.setAlpha(0F);
        }
    }

    /**
     * Resets the air GUI.
     *
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars()V", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 2))
    public void renderAir(CallbackInfo callbackInfo)
    {
        logger.debug("Rendering air");
        /*
         * Resets the color so that nothing else is bothered.
         */
        TimerUtils.setAlpha(1F);
    }

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    public void finishStatus(CallbackInfo info)
    {
        logger.debug("Finishing status bars");
        GlStateManager.popMatrix();
    }

    /**
     * Determines whether or not to draw the mount's health.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountHealth", at = @At(value = "HEAD"),
            cancellable = true)
    public void startMountHealth(CallbackInfo callbackInfo)
    {
        if (TimerUtils.drawMountHealth(MinecraftClient.getInstance().getTickDelta()))
        {
            callbackInfo.cancel();
        }
        else
        {
            logger.debug("Starting mount health");
        }
    }

    /**
     * Resets any changes from drawing the mount's health.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountHealth", at = @At(value = "TAIL"))
    public void finishMountHealth(CallbackInfo callbackInfo)
    {
        logger.debug("Finishing mount health");
        /*
         * Resets the color so that nothing else is bothered.
         */
        GlStateManager.popMatrix();
        TimerUtils.resetAlpha();
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderStatusEffectOverlay()V", at =
    @At(value = "HEAD", shift = At.Shift.AFTER))
    public void startPotion(CallbackInfo info)
    {
        logger.debug("Starting potions");
        TimerUtils.updatePotions(MinecraftClient.getInstance().cameraEntity instanceof ClientPlayerEntity ?
                                 ((ClientPlayerEntity) MinecraftClient.getInstance().cameraEntity) :
                                 null);
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Redirect(method = "renderStatusEffectOverlay()V", at =
    @At(value = "INVOKE",
            target =
                    "Lnet/minecraft/entity/effect/StatusEffectInstance;" +
                            "shouldShowIcon()Z"))
    public boolean shouldRenderPotion(StatusEffectInstance effect)
    {
        logger.debug("Updating potions");
        currentEffect = effect;
        return TimerUtils.updatePotion(effect,
                MinecraftClient.getInstance().getTickDelta());
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @param f - The original transparency
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @ModifyVariable(method = "renderStatusEffectOverlay()V", at =
    @At(value = "STORE"))
    public float updatePotion(float f)
    {
        logger.debug("Adjusting potions");
        return TimerUtils.getPotionAlpha(currentEffect);
    }

    /**
     * Resets any changes from drawing the potion effects.
     *
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.2-1.16.4-fabric
     */
    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "TAIL"))
    public void finishPotion(CallbackInfo callbackInfo)
    {
        logger.debug("Finishing potions");
        /*
         * Resets the color so that nothing else is bothered.
         */
        TimerUtils.resetAlpha();
    }

    /**
     * Determines whether or not to draw the hotbar.
     *
     * @param tickDelta
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderHotbar", at = @At(value = "HEAD"), cancellable =
            true)
    public void startHotbar(float tickDelta,
                            CallbackInfo callbackInfo)
    {
        logger.debug("Starting hotbar");
        if (TimerUtils.updateHotbar(tickDelta))
        {
            callbackInfo.cancel();
        }
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @param tickDelta
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target =
            "Lcom/mojang/blaze3d/platform/GlStateManager;color4f(FFFF)V", shift
            = At.Shift.AFTER))
    public void adjustHotbar(float tickDelta,
                             CallbackInfo callbackInfo)
    {
        logger.debug("Adjusting hotbar");
        TimerUtils.recolorHotbar();
    }

    /**
     * Resets any changes from drawing the hotbar.
     *
     * @param tickDelta
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderHotbar", at = @At(value = "TAIL"))
    public void finishHotbar(float tickDelta,
                             CallbackInfo callbackInfo)
    {
        logger.debug("Finishing hotbar");
        GlStateManager.popMatrix();
        /*
         * Resets the color so that nothing else is bothered.
         */
        TimerUtils.resetAlpha();
    }
}
