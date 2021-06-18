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

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
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
    private static final Logger LOGGER = LogManager.getLogger(HudHook.class);

    private StatusEffectInstance currentEffect;

    /**
     * Determines whether or not to draw the crosshair.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"),
            cancellable = true)
    public void startCrosshair(MatrixStack stack, CallbackInfo callbackInfo)
    {
        try
        {
            if (TimerUtils.drawCrosshair(MinecraftClient.getInstance()
                    .getTickDelta()))
            {
                callbackInfo.cancel();
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#startCrosshair " +
                            "event",
                    e);
        }
    }

    /**
     * Resets any changes from drawing the crosshair.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderCrosshair", at = @At(value = "TAIL"))
    public void finishCrosshair(MatrixStack stack, CallbackInfo callbackInfo)
    {
        try
        {
            /*
             * Resets the color so that nothing else is bothered.
             */
            TimerUtils.resetAlpha();
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderCrosshair " +
                            "event",
                    e);
        }
    }

    /**
     * Determines whether or not to draw the horse jump bar.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountJumpBar", at = @At(value = "HEAD"),
            cancellable = true)
    public void startJumpbar(MatrixStack stack,
                             int x,
                             CallbackInfo callbackInfo)
    {
        try
        {
            if (TimerUtils.drawJumpbar(stack,
                    MinecraftClient.getInstance().getTickDelta()))
            {
                callbackInfo.cancel();
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderMountJumpBar " +
                            "event",
                    e);
        }
    }

    /**
     * Resets any changes from drawing the horse jump bar.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountJumpBar", at = @At(value = "TAIL"))
    public void finishJumpbar(MatrixStack stack,
                              int x,
                              CallbackInfo callbackInfo)
    {
        try
        {
            stack.pop();
            /*
             * Resets the color so that nothing else is bothered.
             */
            TimerUtils.resetAlpha();
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderMountJumpBar " +
                            "event",
                    e);
        }
    }

    /**
     * Determines whether or not to draw the experience bar.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderExperienceBar", at = @At(value = "HEAD"),
            cancellable = true)
    public void startExperience(MatrixStack stack,
                                int x,
                                CallbackInfo callbackInfo)
    {
        try
        {
            if (TimerUtils.drawExperience(stack,
                    MinecraftClient.getInstance().getTickDelta()))
            {
                callbackInfo.cancel();
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderExperienceBar " +
                            "event",
                    e);
        }
    }

    /**
     * Resets any changes from drawing the experience bar.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderExperienceBar", at = @At(value = "TAIL"))
    public void finishExperience(MatrixStack stack,
                                 int x,
                                 CallbackInfo callbackInfo)
    {
        try
        {
            stack.pop();
            /*
             * Resets the color so that nothing else is bothered.
             */
            TimerUtils.resetAlpha();
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderExperienceBar " +
                            "event",
                    e);
        }
    }

    /**
     * Changes the position of the status bars as the ones below them move up
     * and down.
     *
     * @param stack - The matrix stack used.
     *
     * @return The same matrix stack, with a new matrix pushed onto it.
     */
    @ModifyVariable(method = "renderStatusBars(Lnet/minecraft/client/util" +
            "/math/MatrixStack;)V", at = @At("HEAD"))
    public MatrixStack startStatus(MatrixStack stack)
    {
        try
        {
            stack.push();
            stack.translate(0F, TimerUtils.getHealthTranslation(), 0F);
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusBars " +
                            "event",
                    e);
        }
        return stack;
    }

    /**
     * Adjusts the armor GUI.
     *
     * @param matrices
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars(Lnet/minecraft/client/util" +
            "/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"))
    public void renderArmor(MatrixStack matrices, CallbackInfo callbackInfo)
    {
        try
        {
            if (TimerUtils.drawArmor())
            {
                TimerUtils.setAlpha(0F);
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusBars " +
                            "event",
                    e);
        }
    }

    /**
     * Adjusts the health GUI.
     *
     * @param matrices
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars(Lnet/minecraft/client/util" +
            "/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 0))
    public void renderHealth(MatrixStack matrices, CallbackInfo callbackInfo)
    {
        try
        {
            /*
             * Resets the transparency from the previous call.
             */
            TimerUtils.setAlpha(1F);
            if (TimerUtils.drawHealth(MinecraftClient.getInstance()
                    .getTickDelta()))
            {
                TimerUtils.setAlpha(0F);
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusBars " +
                            "event",
                    e);
        }
    }

    /**
     * Adjusts the food GUI.
     *
     * @param matrices
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars(Lnet/minecraft/client/util" +
            "/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 1))
    public void renderFood(MatrixStack matrices, CallbackInfo callbackInfo)
    {
        try
        {
            /*
             * Resets the transparency from the previous call.
             */
            TimerUtils.setAlpha(1F);
            if (TimerUtils.drawHunger(MinecraftClient.getInstance()
                    .getTickDelta()))
            {
                TimerUtils.setAlpha(0F);
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusBars " +
                            "event",
                    e);
        }
    }

    /**
     * Resets the air GUI.
     *
     * @param matrices
     * @param callbackInfo
     */
    @Inject(method = "renderStatusBars(Lnet/minecraft/client/util" +
            "/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 2))
    public void renderAir(MatrixStack matrices, CallbackInfo callbackInfo)
    {
        try
        {
            /*
             * Resets the color so that nothing else is bothered.
             */
            TimerUtils.setAlpha(1F);
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusBars " +
                            "event",
                    e);
        }
    }

    @ModifyVariable(method = "renderStatusBars(Lnet/minecraft/client/util" +
            "/math/MatrixStack;)V", at = @At("TAIL"))
    public MatrixStack finishStatus(MatrixStack stack)
    {
        try
        {
            stack.pop();
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusBars " +
                            "event",
                    e);
        }
        return stack;
    }

    /**
     * Determines whether or not to draw the mount's health.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountHealth", at = @At(value = "HEAD"),
            cancellable = true)
    public void startMountHealth(MatrixStack stack, CallbackInfo callbackInfo)
    {
        try
        {
            if (TimerUtils.drawMountHealth(stack,
                    MinecraftClient.getInstance().getTickDelta()))
            {
                callbackInfo.cancel();
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderMountHealth " +
                            "event",
                    e);
        }
    }

    /**
     * Resets any changes from drawing the mount's health.
     *
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderMountHealth", at = @At(value = "TAIL"))
    public void finishMountHealth(MatrixStack stack, CallbackInfo callbackInfo)
    {
        try
        {
            /*
             * Resets the color so that nothing else is bothered.
             */
            stack.pop();
            TimerUtils.resetAlpha();
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderMountHealth " +
                            "event",
                    e);
        }
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderStatusEffectOverlay" +
            "(Lnet/minecraft/client/util/math/MatrixStack;)V", at =
    @At(value = "HEAD", shift = At.Shift.AFTER))
    public void startPotion(MatrixStack effect, CallbackInfo info)
    {
        try
        {
            TimerUtils.updatePotions(MinecraftClient.getInstance().cameraEntity instanceof ClientPlayerEntity ?
                                     ((ClientPlayerEntity) MinecraftClient.getInstance().cameraEntity) :
                                     null);
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusEffectOverlay " +
                            "event",
                    e);
        }
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Redirect(method = "renderStatusEffectOverlay" +
            "(Lnet/minecraft/client/util/math/MatrixStack;)V", at =
    @At(value = "INVOKE",
            target =
                    "Lnet/minecraft/entity/effect/StatusEffectInstance;" +
                            "shouldShowIcon()Z"))
    public boolean shouldRenderPotion(StatusEffectInstance effect)
    {
        try
        {
            currentEffect = effect;
            return TimerUtils.updatePotion(effect,
                    MinecraftClient.getInstance().getTickDelta());
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusEffectOverlay " +
                            "event",
                    e);
        }
        return true;
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @param f - The original transparency
     * @param stack - The matrix stack
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @ModifyVariable(method = "renderStatusEffectOverlay" +
            "(Lnet/minecraft/client/util/math/MatrixStack;)V", at =
    @At(value = "STORE"))
    public float updatePotion(float f,
                              MatrixStack stack)
    {
        try
        {
            return TimerUtils.getPotionAlpha(currentEffect);
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusEffectOverlay " +
                            "event",
                    e);
        }
        return 1F;
    }

    /**
     * Resets any changes from drawing the potion effects.
     *
     * @param matrices - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.2-1.16.4-fabric
     */
    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "TAIL"))
    public void finishPotion(MatrixStack matrices,
                             CallbackInfo callbackInfo)
    {
        try
        {
            /*
             * Resets the color so that nothing else is bothered.
             */
            TimerUtils.resetAlpha();
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderStatusEffectOverlay " +
                            "event",
                    e);
        }
    }

    /**
     * Determines whether or not to draw the hotbar.
     *
     * @param tickDelta
     * @param matrices - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderHotbar", at = @At(value = "HEAD"), cancellable =
            true)
    public void startHotbar(float tickDelta,
                            MatrixStack matrices,
                            CallbackInfo callbackInfo)
    {
        try
        {
            if (TimerUtils.updateHotbar(tickDelta))
            {
                callbackInfo.cancel();
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderHotbar " +
                            "event",
                    e);
        }
    }

    /**
     * Makes the actual adjustment to the hotbar as needed.
     *
     * @param tickDelta
     * @param matrices - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target =
            "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", shift
            = At.Shift.AFTER))
    public void adjustHotbar(float tickDelta,
                             MatrixStack matrices,
                             CallbackInfo callbackInfo)
    {
        try
        {
            TimerUtils.recolorHotbar(matrices);
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderHotbar " +
                            "event",
                    e);
        }
    }

    /**
     * Resets any changes from drawing the hotbar.
     *
     * @param tickDelta
     * @param stack - The matrix drawing stack.
     * @param callbackInfo
     *
     * @version 0.2-1.16.4-fabric
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderHotbar", at = @At(value = "TAIL"))
    public void finishHotbar(float tickDelta,
                             MatrixStack stack,
                             CallbackInfo callbackInfo)
    {
        try
        {
            stack.pop();
            /*
             * Resets the color so that nothing else is bothered.
             */
            TimerUtils.resetAlpha();
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.gui.hud" +
                            ".InGameHud#renderHotbar " +
                            "event",
                    e);
        }
    }
}