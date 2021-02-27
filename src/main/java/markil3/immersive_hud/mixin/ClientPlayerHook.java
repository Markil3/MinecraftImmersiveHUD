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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import markil3.immersive_hud.TimerUtils;

/**
 * This mixin hooks into the client player class to check for when the hands
 * move. Note that most of the logic can be found in {@link TimerUtils}.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-fabric
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerHook
{
    /**
     * Updates hand timing every time an attack is initiated.
     *
     * @param hand - The attacking hand.
     * @param callbackInfo
     *
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "swingHand", at = @At(value = "TAIL"))
    public void swingHand(Hand hand, CallbackInfo callbackInfo)
    {
        TimerUtils.onClick(hand, hand == null);
    }

    /**
     * Updates hand timing every time an item is used.
     *
     * @param hand - The using hand.
     * @param callbackInfo
     *
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "setCurrentHand", at = @At(value = "TAIL"))
    public void setCurrentHand(Hand hand, CallbackInfo callbackInfo)
    {
        TimerUtils.onClick(hand, hand == null);
    }

    /**
     * Updates hand timing every the hand is no longer being used.
     *
     * @param callbackInfo
     *
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "clearActiveItem", at = @At(value = "HEAD"))
    public void clearActiveItem(CallbackInfo callbackInfo)
    {
        if (MinecraftClient.getInstance().cameraEntity instanceof LivingEntity)
        {
            TimerUtils.onClick(((LivingEntity) MinecraftClient.getInstance().cameraEntity)
                    .getActiveHand(), true);
        }
    }
}
