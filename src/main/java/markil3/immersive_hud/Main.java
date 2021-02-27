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

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main entry point for the Immersive HUD mod.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-forge
 */
@Mod("immersive_hud")
public class Main
{
    public static final int FADE_IN_TIME = 5;
    public static final int FADE_OUT_TIME = 20;
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    public Main()
    {
        //Make sure the mod being absent on the other network side does not
        // cause the client to display the server as incompatible
        ModLoadingContext.get()
                .registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair
                        .of(() -> FMLNetworkConstants.IGNORESERVERONLY,
                                (a, b) -> true));
    }

    /**
     * A utility method to handle the transparency timing. It will (for the most
     * part), make
     *
     * @param renderTime - How many ticks until the element entirely
     * disappears.
     * @param maxTime - How many ticks the element can appear.
     *
     * @return The proper transparency to render something.
     */
    static float getAlpha(int renderTime, int maxTime)
    {
        if (renderTime <= FADE_OUT_TIME)
        {
            return renderTime / (float) FADE_OUT_TIME;
        }
        else if (renderTime > maxTime - FADE_IN_TIME)
        {
            return (maxTime - renderTime) / (float) FADE_IN_TIME;
        }
        return 1F;
    }
}
