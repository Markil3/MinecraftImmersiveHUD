package markil3.immersive_hud;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;

@Config(name = "immersive_hud")
public class ConfigManagerCloth extends ConfigManager implements ConfigData
{
    @Override
    public void validatePostLoad() throws ConfigData.ValidationException
    {
        this.setMinHealth(this.getMinHealth());
    }
}
