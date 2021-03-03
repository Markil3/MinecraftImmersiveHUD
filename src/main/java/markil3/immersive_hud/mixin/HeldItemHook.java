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
import net.minecraft.client.render.FirstPersonRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

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
@Mixin(FirstPersonRenderer.class)
public class HeldItemHook
{
    /**
     * Determines whether or not to render a certain hand.
     *
     * @param hand
     * @param callbackInfo
     *
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "renderFirstPersonItem" +
            "(Lnet/minecraft/client/network/AbstractClientPlayerEntity;" +
            "FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;F)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform" +
                    "/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER),
            cancellable = true)
    public void renderHand(AbstractClientPlayerEntity abstractClientPlayerEntity,
                           float f,
                           float g,
                           Hand hand,
                           float h,
                           ItemStack itemStack,
                           float i,
                           CallbackInfo callbackInfo)
    {
        if (TimerUtils.onRenderHand(hand, f))
        {
            callbackInfo.cancel();
        }
    }

}
