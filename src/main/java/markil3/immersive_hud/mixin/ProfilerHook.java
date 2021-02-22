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

import net.minecraft.util.profiler.DummyProfiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import markil3.immersive_hud.EventBus;

/**
 * This class hooks into the profiler to trigger color changes for the status
 * indicators.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-fabric
 */
@Mixin(DummyProfiler.class)
public class ProfilerHook
{
    /**
     * Hooks into the profiler to add logic to drawing armor.
     *
     * @param section - The profiler section
     * @param callbackInfo
     *
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "push", at = @At(value = "TAIL"))
    public void renderArmor(String section, CallbackInfo callbackInfo)
    {
        if (section.equals("armor"))
        {
            EventBus.drawArmor();
        }
    }

    /**
     * Hooks into the profiler to add logic to drawing status bars besides
     * armor.
     *
     * @param section - The profiler section
     * @param callbackInfo
     *
     * @since 0.1-1.16.4-fabric
     */
    @Inject(method = "swap", at = @At(value = "TAIL"))
    public void renderStatus(String section, CallbackInfo callbackInfo)
    {
        switch (section)
        {
        case "health":
            EventBus.drawHealth();
            break;
        case "food":
            EventBus.drawHunger();
            break;
        case "air":
            /*
             * Resets the color so that nothing else is bothered.
             */
            RenderSystem.color4f(1, 1, 1, 1F);
            break;
        }
    }
}
