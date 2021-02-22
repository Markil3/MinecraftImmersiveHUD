/**
 * This Minecraft mod aims to move the ingame HUD out of the way whenever
 * possible. Copyright (C) 2021 Markil 3
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

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

/**
 * The main entry point for the Immersive HUD mod.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-fabric
 */
public class Main implements ModInitializer
{
    @Override
    public void onInitialize()
    {
    }

    /**
     * A utility method to handle the transparency timing. It will (for the most
     * part), make
     *
     * @param renderTime - How many ticks until the element entirely
     * disappears.
     *
     * @return The new alpha value.
     *
     * @since 0.1-1.16.4-fabric
     */
    static float getAlpha(int renderTime)
    {
        return Math.min(renderTime / 20.0F,
                1.0F);
    }
}
