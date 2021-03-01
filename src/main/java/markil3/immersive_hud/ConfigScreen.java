package markil3.immersive_hud;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigScreen extends Screen
{
    /**
     * Distance from top of the screen to this GUI's title
     */
    private static final int TITLE_HEIGHT = 8;

    protected ConfigScreen()
    {
        super(new TranslationTextComponent("immersive_hud.configGui.title"));
    }

    @Override
    public void render(MatrixStack matrixStack,
                       int mouseX,
                       int mouseY,
                       float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.drawCenteredString(matrixStack,
                this.font,
                this.title.getString(),
                this.width / 2,
                TITLE_HEIGHT,
                0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
