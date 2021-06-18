package markil3.immersive_hud;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "immersive_hud")
public class ConfigManagerCloth extends ConfigManager implements ConfigData
{
    @Override
    public void validatePostLoad() throws ValidationException
    {
        this.setMinHealth(this.getMinHealth());
    }
}
