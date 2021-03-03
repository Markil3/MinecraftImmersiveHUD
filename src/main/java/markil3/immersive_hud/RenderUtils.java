/**
 * Copyright (C) 2009-2021 Mojang Studios
 * <p>
 * Players are free to create, build, and mod within Minecraft if the branding
 * usage guidelines are followed. Fan art, logos, videos, and screen shots are
 * acceptable as long as they are not used to falsely represent Mojang Minecraft
 * assets.  If you intend to use any portion of the name in relation to
 * services, products, or distribution, you must adhere to the following
 * requirements.
 * <p>
 * Mojang's terms of service and brand guidelines are designed to let you know
 * in plain terms what you can and cannot do with your game, the Mojang brand or
 * Mojang assets. https://account.mojang.com/documents/minecraft_eula
 */
package markil3.immersive_hud;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Contains methods for rendering various HUD elements. Most of these methods
 * come from {@link IngameGui} and
 * {@link net.minecraft.client.renderer.ItemRenderer},
 * with some tweaks to make the mod work.
 *
 * @author Mojang Studios
 * @version 0.1-1.16.4-forge
 */
public class RenderUtils
{
    private static int getBlitOffset(IngameGui gui)
    {
        Field blitAccessField = null;
        int blitAccess = 0;
        try
        {
            blitAccessField = AbstractGui.class.getDeclaredField("blitOffset");
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        if (!blitAccessField.isAccessible())
        {
            blitAccessField.setAccessible(true);
        }
        try
        {
            blitAccess = (int) blitAccessField.get(gui);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return blitAccess;
    }

    private static boolean setBlitOffset(IngameGui gui, int value)
    {
        Field blitAccessField = null;
        int blitAccess = 0;
        try
        {
            blitAccessField = AbstractGui.class.getDeclaredField("blitOffset");
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            return false;
        }

        if (!blitAccessField.isAccessible())
        {
            blitAccessField.setAccessible(true);
        }
        try
        {
            blitAccessField.set(gui, value);
            return true;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static void renderPotionIcons(Minecraft mc,
                                         IngameGui gui,
                                         float ticks)
    {

        int scaledWidth = mc.mainWindow.getScaledWidth();
        int scaledHeight = mc.mainWindow.getScaledHeight();

        Collection<EffectInstance> collection =
                mc.player.getActivePotionEffects();


        if (!collection.isEmpty())
        {
            GlStateManager.enableBlend();
            int i = 0;
            int j = 0;
            PotionSpriteUploader potionspriteuploader =
                    mc.getPotionSpriteUploader();
            List<Runnable> list =
                    Lists.newArrayListWithExpectedSize(collection.size());
            mc.getTextureManager()
                    .bindTexture(ContainerScreen.INVENTORY_BACKGROUND);

            for (EffectInstance effectinstance : Ordering.natural()
                    .reverse()
                    .sortedCopy(collection))
            {
                Effect effect = effectinstance.getPotion();
                float effectAlpha = TimerUtils.getPotionAlpha(mc.player,
                        effectinstance,
                        ticks);
                if (!effectinstance.isShowIcon() || effectAlpha < 0.01F)
                {
                    continue;
                }
                // Rebind in case previous renderHUDEffect changed texture
                mc.getTextureManager()
                        .bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
                if (effectinstance.isShowIcon())
                {
                    int k = scaledWidth;
                    int l = 1;
                    if (mc.isDemo())
                    {
                        l += 15;
                    }

                    if (effect.isBeneficial())
                    {
                        ++i;
                        k = k - 25 * i;
                    }
                    else
                    {
                        ++j;
                        k = k - 25 * j;
                        l += 26;
                    }

                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, effectAlpha);
                    if (effectinstance.isAmbient())
                    {
                        gui.blit(k, l, 165, 166, 24, 24);
                    }
                    else
                    {
                        gui.blit(k, l, 141, 166, 24, 24);
                    }

                    float f_f = effectAlpha;
                    int k_f = k;
                    int l_f = l;

                    TextureAtlasSprite textureatlassprite =
                            potionspriteuploader.func_215288_a(effect);
                    list.add(() -> {
                        GlStateManager.color4f(1.0F, 1.0F, 1.0F, f_f);
                        gui.blit(k_f + 3, l_f + 3, getBlitOffset(gui), 18, 18, textureatlassprite);
                    });
                    effect.renderHUDEffect(effectinstance, gui,
                            k,
                            l,
                            getBlitOffset(gui),
                            effectAlpha);
                }
            }

            mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_EFFECTS_TEXTURE);
            list.forEach(Runnable::run);
        }
    }

    /**
     * Renders the horse jump bar.
     *
     * @param mc - A Minecraft instance.
     * @param gui - The GUI.
     * @param ticks
     * @param renderTime - How much time before this element becomes fully
     * transparent.
     *
     * @see IngameGui#renderHorseJumpBar(int)
     */
    public static void renderHorseJumpBar(Minecraft mc,
                                          IngameGui gui,
                                          float ticks, double renderTime)
    {
        ConfigManager.TimeValues jump =
                ConfigManager.getInstance().getJumpTime();
        if (renderTime <= 0)
        {
            return;
        }

        float alpha = Main.getAlpha(renderTime,
                jump.getMaxTime(),
                jump.getFadeInTime(),
                jump.getFadeOutTime());
        int scaledWidth = mc.mainWindow.getScaledWidth();
        int scaledHeight = mc.mainWindow.getScaledHeight();
        int xPosition = scaledWidth / 2 - 91;

        mc.getProfiler().startSection("jumpBar");
        GlStateManager.enableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
        float f = mc.player.getHorseJumpPower();
        int i = 182;
        int j = (int) (f * 183.0F);
        int k = scaledHeight - TimerUtils.getJumpTranslation();
        gui.blit(xPosition, k, 0, 84, 182, 5);
        if (j > 0)
        {
            gui.blit(xPosition, k, 0, 89, j, 5);
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableBlend();
        mc.getProfiler().endSection();
    }

    /**
     * Renders the experience bar.
     *
     * @param mc - A Minecraft instance.
     * @param gui - The GUI.
     * @param ticks
     * @param renderTime - How much time before this element becomes fully
     * transparent.
     *
     * @see IngameGui#func_238454_b_(int)
     */
    public static void renderExperience(Minecraft mc,
                                        IngameGui gui,
                                        float ticks, double renderTime)
    {
        ConfigManager.TimeValues experience =
                ConfigManager.getInstance().getHealthTime();
        ConfigManager.TimeValues hotbar =
                ConfigManager.getInstance().getHotbarTime();
        if (renderTime <= 0)
        {
            return;
        }

        float alpha = Main.getAlpha(renderTime,
                experience.getMaxTime(),
                experience.getFadeInTime(),
                experience.getFadeOutTime());
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);

        if (mc.playerController.gameIsSurvivalOrAdventure())
        {
            int scaledWidth = mc.mainWindow.getScaledWidth();
            int scaledHeight = mc.mainWindow.getScaledHeight();
            int x = scaledWidth / 2 - 91;

            mc.getProfiler().startSection("expBar");
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
            int i = mc.player.xpBarCap();
            if (i > 0)
            {
                int j = 182;
                int k = (int) (mc.player.experience * 183.0F);
                int l = scaledHeight - TimerUtils.getExperienceTranslation();
                gui.blit(x, l, 0, 64, 182, 5);
                if (k > 0)
                {
                    gui.blit(x, l, 0, 69, k, 5);
                }
            }

            GlStateManager.disableBlend();
            mc.getProfiler().endSection();
            if (mc.player.experienceLevel > 0)
            {
                mc.getProfiler().startSection("expLevel");
                GlStateManager.enableBlend();
                String s = "" + mc.player.experienceLevel;
                int i1 = (scaledWidth - gui.getFontRenderer()
                        .getStringWidth(s)) / 2;
                int j1 =
                        scaledHeight - (int) ((22F * Main.getAlpha(TimerUtils.hotbarTime,
                                hotbar.getMaxTime(),
                                hotbar.getFadeInTime(),
                                hotbar.getFadeOutTime()) + 9F) * alpha) - (int) (4F * alpha);
                gui.getFontRenderer()
                        .drawString(s,
                                (float) (i1 + 1),
                                (float) j1,
                                0);
                gui.getFontRenderer()
                        .drawString(s,
                                (float) (i1 - 1),
                                (float) j1,
                                0);
                gui.getFontRenderer()
                        .drawString(s,
                                (float) i1,
                                (float) (j1 + 1),
                                0);
                gui.getFontRenderer()
                        .drawString(s,
                                (float) i1,
                                (float) (j1 - 1),
                                0);
                gui.getFontRenderer()
                        .drawString(s,
                                (float) i1,
                                (float) j1,
                                8453920);
                GlStateManager.disableBlend();
                mc.getProfiler().endSection();
            }
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }


    /**
     * Renders the hotbar and all the items in it.
     *
     * @param mc - A Minecraft instance.
     * @param gui - The GUI.
     * @param partialTicks
     * @param renderTime - How much time before this element becomes fully
     * transparent.
     *
     * @see IngameGui#renderHotbar(float)
     */
    public static void renderHotbar(Minecraft mc,
                                    IngameGui gui,
                                    float partialTicks,
                                    double renderTime)
    {
        ConfigManager.TimeValues hotbar =
                ConfigManager.getInstance().getHotbarTime();
        final ResourceLocation WIDGETS_TEX_PATH =
                new ResourceLocation("textures/gui/widgets.png");
        PlayerEntity playerentity = mc.player;

        if (renderTime <= 0)
        {
            return;
        }

        float alpha = Main.getAlpha(renderTime,
                hotbar.getMaxTime(),
                hotbar.getFadeInTime(),
                hotbar.getFadeOutTime());

        int scaledWidth = mc.mainWindow.getScaledWidth();
        int scaledHeight = mc.mainWindow.getScaledHeight();
        if (playerentity != null)
        {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);
            ItemStack itemstack = playerentity.getHeldItemOffhand();
            HandSide handside = playerentity.getPrimaryHand().opposite();
            int i = scaledWidth / 2;
            int j = getBlitOffset(gui);
            int k = 182;
            int l = 91;
            setBlitOffset(gui, -90);
            gui.blit(i - 91, scaledHeight - TimerUtils.getHotbarTranslation(), 0, 0, 182, 22);
            gui.blit(i - 91 - 1 + playerentity.inventory.currentItem * 20,
                    scaledHeight - (int) (22F * alpha) - 1,
                    0,
                    22,
                    24,
                    22);
            if (!itemstack.isEmpty())
            {
                if (handside == HandSide.LEFT)
                {
                    gui.blit(i - 91 - 29,
                            scaledHeight - (int) (23 * alpha),
                            24,
                            22,
                            29,
                            24);
                }
                else
                {
                    gui.blit(i + 91, scaledHeight - (int) (23 * alpha), 53, 22, 29, 24);
                }
            }

            setBlitOffset(gui, j);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();

            for (int i1 = 0; i1 < 9; ++i1)
            {
                int j1 = i - 90 + i1 * 20 + 2;
                int k1 = scaledHeight - (int) (16 * alpha) - 3;
                renderHotbarItem(mc,
                        alpha,
                        j1,
                        k1,
                        partialTicks,
                        playerentity,
                        playerentity.inventory.mainInventory.get(i1));
            }

            if (!itemstack.isEmpty())
            {
                int i2 = scaledHeight - (int) (16 * alpha) - 3;
                if (handside == HandSide.LEFT)
                {
                    renderHotbarItem(mc,
                            alpha,
                            i - 91 - 26,
                            i2,
                            partialTicks,
                            playerentity,
                            itemstack);
                }
                else
                {
                    renderHotbarItem(mc,
                            alpha,
                            i + 91 + 10,
                            i2,
                            partialTicks,
                            playerentity,
                            itemstack);
                }
            }

            if (mc.gameSettings.attackIndicator == AttackIndicatorStatus.HOTBAR)
            {
                float f = mc.player.getCooledAttackStrength(0.0F);
                if (f < 1.0F)
                {
                    int j2 = scaledHeight - 20;
                    int k2 = i + 91 + 6;
                    if (handside == HandSide.RIGHT)
                    {
                        k2 = i - 91 - 22;
                    }

                    mc.getTextureManager()
                            .bindTexture(AbstractGui.GUI_ICONS_LOCATION);
                    int l1 = (int) (f * 19.0F);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    gui.blit(k2, j2, 0, 94, 18, 18);
                    gui.blit(k2, j2 + 18 - l1, 18, 112 - l1, 18, l1);
                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    }

    /**
     * Renders a single item on the HUD.
     *
     * @param mc - A Minecraft instance.
     * @param x - The x position of the item on the screen.
     * @param y - The y position of the item on the screen
     * @param partialTicks
     * @param player - The player that the item is from.
     * @param stack - The item to render.
     *
     * @see IngameGui#renderHotbarItem(int, int, float, PlayerEntity, ItemStack)
     */
    static void renderHotbarItem(Minecraft mc, float alpha,
                                 int x,
                                 int y,
                                 float partialTicks,
                                 PlayerEntity player,
                                 ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
            float f = (float) stack.getAnimationsToGo() - partialTicks;
            if (f > 0.0F)
            {
                GlStateManager.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                GlStateManager.translatef((float) (x + 8),
                        (float) (y + 12),
                        0.0F);
                GlStateManager.scalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translatef((float) (-(x + 8)),
                        (float) (-(y + 12)),
                        0.0F);
            }

            renderItemAndEffectIntoGUI(mc, player, stack, x, y, alpha);
            if (f > 0.0F)
            {
                GlStateManager.popMatrix();
            }

            mc.getItemRenderer()
                    .renderItemOverlays(mc.fontRenderer, stack, x, y);
        }
    }

    private static void renderItemAndEffectIntoGUI(Minecraft mc,
                                                   LivingEntity entityIn,
                                                   ItemStack itemIn,
                                                   int x,
                                                   int y,
                                                   float alpha)
    {
        if (!itemIn.isEmpty())
        {
            float zLevel = 50.0F;

            try
            {
                IBakedModel bakedmodel = mc.getItemRenderer()
                        .getItemModelWithOverrides(itemIn,
                                (World) null,
                                entityIn);
                GlStateManager.pushMatrix();
                mc.getTextureManager()
                        .bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                mc.getTextureManager()
                        .getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                        .setBlurMipmap(false, false);
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableAlphaTest();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
                Method transformMethod = null;
                try
                {
                    transformMethod = ItemRenderer.class.getDeclaredMethod("setupGuiTransform", int.class, int.class, boolean.class);
                }
                catch (NoSuchMethodException e)
                {
                    try
                    {
                        transformMethod = ItemRenderer.class.getDeclaredMethod("func_180452_a", int.class, int.class, boolean.class);
                    }
                    catch (NoSuchMethodException e1)
                    {
                        e.printStackTrace();
                    }
                }
                if (transformMethod != null)
                {
                    if (!transformMethod.isAccessible())
                    {
                        transformMethod.setAccessible(true);
                    }
                    transformMethod.invoke(mc.getItemRenderer(), x, y, bakedmodel.isGui3d());
                }
                bakedmodel =
                        net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(
                                bakedmodel,
                                ItemCameraTransforms.TransformType.GUI,
                                false);
                mc.getItemRenderer().renderItem(itemIn, bakedmodel);
                GlStateManager.disableAlphaTest();
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableLighting();
                GlStateManager.popMatrix();
                mc.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                mc.textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                        .restoreLastBlurMipmap();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable,
                        "Rendering item");
                CrashReportCategory crashreportcategory =
                        crashreport.makeCategory("Item being rendered");
                crashreportcategory.addDetail("Item Type", () -> {
                    return String.valueOf((Object) itemIn.getItem());
                });
                crashreportcategory.addDetail("Registry Name",
                        () -> String.valueOf(itemIn.getItem()
                                .getRegistryName()));
                crashreportcategory.addDetail("Item Damage", () -> {
                    return String.valueOf(itemIn.getDamage());
                });
                crashreportcategory.addDetail("Item NBT", () -> {
                    return String.valueOf((Object) itemIn.getTag());
                });
                crashreportcategory.addDetail("Item Foil", () -> {
                    return String.valueOf(itemIn.hasEffect());
                });
                throw new ReportedException(crashreport);
            }
        }
    }
}
