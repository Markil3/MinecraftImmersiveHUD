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

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;

/**
 * The main entry point for the Immersive HUD mod.
 *
 * @author Markil 3
 * @version 0.1-1.16.4-fabric
 */
public class Main implements ModInitializer
{
    public static final int TICKS_PER_SECOND = 20;

    @Override
    public void onInitialize()
    {
        try
        {
            if (Class.forName("me.sargunvohra.mcmods.autoconfig1u.AutoConfig") != null)
            {
                AutoConfig.register(ConfigManagerCloth.class,
                        Toml4jConfigSerializer::new);
            }
        }
        catch (ClassNotFoundException e)
        {
            // Do nothing
        }
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
    static float getAlpha(double renderTime,
                          double maxTime,
                          double fadeInTime,
                          double fadeOutTime)
    {
        if (renderTime <= fadeOutTime)
        {
            return (float) (renderTime / fadeOutTime);
        }
        else if (renderTime > maxTime - fadeInTime)
        {
            return (float) ((maxTime - renderTime) / fadeInTime);
        }
        return 1F;
    }
}
