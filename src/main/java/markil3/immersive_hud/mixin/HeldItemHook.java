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

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import markil3.immersive_hud.TimerUtils;

/**
 * This mixin hooks into the hand renderer to make the hands move in and out of
 * view. Note that most of the logic can be found in {@link TimerUtils}.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-fabric
 */
@Mixin(HeldItemRenderer.class)
public class HeldItemHook
{
    private static final Logger LOGGER =
            LogManager.getLogger(HeldItemHook.class);

    /**
     * Determines whether or not to render a certain hand.
     *
     * @param player
     * @param tickDelta
     * @param pitch
     * @param hand
     * @param swingProgress
     * @param item
     * @param equipProgress
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param callbackInfo
     *
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
            shift = At.Shift.AFTER), cancellable = true)
    public void renderHand(AbstractClientPlayerEntity player,
                           float tickDelta,
                           float pitch,
                           Hand hand,
                           float swingProgress,
                           ItemStack item,
                           float equipProgress,
                           MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers,
                           int light,
                           CallbackInfo callbackInfo)
    {
        try
        {
            if (TimerUtils.onRenderHand(hand, matrices, tickDelta))
            {
                callbackInfo.cancel();
            }
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Error in running net.minecraft.client.render.item" +
                            ".HeldItemRenderer#renderFirstPersonItem " +
                            "event",
                    e);
        }
    }

}
