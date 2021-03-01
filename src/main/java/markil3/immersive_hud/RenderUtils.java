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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

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
    public static void renderPotionIcons(Minecraft mc,
                                         IngameGui gui,
                                         MatrixStack matrixStack, float ticks)
    {
        int scaledWidth = mc.getMainWindow().getScaledWidth();
        int scaledHeight = mc.getMainWindow().getScaledHeight();

        Collection<EffectInstance> collection =
                mc.player.getActivePotionEffects();
        if (!collection.isEmpty())
        {
            RenderSystem.enableBlend();
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
                float effectAlpha = TimerUtils.getPotionAlpha(mc.player, effectinstance, ticks);
                if (!effectinstance.shouldRenderHUD() || effectAlpha < 0.01F)
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

                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, effectAlpha);
                    if (effectinstance.isAmbient())
                    {
                        gui.blit(matrixStack, k, l, 165, 166, 24, 24);
                    }
                    else
                    {
                        gui.blit(matrixStack, k, l, 141, 166, 24, 24);
                    }

                    TextureAtlasSprite textureatlassprite =
                            potionspriteuploader.getSprite(effect);
                    int j1 = k;
                    int k1 = l;
                    float f1 = effectAlpha;
                    list.add(() -> {
                        mc.getTextureManager()
                                .bindTexture(textureatlassprite.getAtlasTexture()
                                        .getTextureLocation());
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, f1);
                        gui.blit(matrixStack,
                                j1 + 3,
                                k1 + 3,
                                gui.getBlitOffset(),
                                18,
                                18,
                                textureatlassprite);
                    });
                    effectinstance.renderHUDEffect(gui,
                            matrixStack,
                            k,
                            l,
                            gui.getBlitOffset(),
                            effectAlpha);
                }
            }

            list.forEach(Runnable::run);
        }
    }

    /**
     * Renders the horse jump bar.
     *
     * @param mc - A Minecraft instance.
     * @param gui - The GUI.
     * @param matrixStack - The rendering transformation matrix stack.
     * @param ticks
     * @param renderTime - How much time before this element becomes fully
     * transparent.
     *
     * @see IngameGui#renderHorseJumpBar(MatrixStack, int)
     */
    public static void renderHorseJumpBar(Minecraft mc,
                                          IngameGui gui,
                                          MatrixStack matrixStack,
                                          float ticks, double renderTime)
    {
        ConfigManager.TimeValues jump = ConfigManager.getInstance().getJumpTime();
        if (renderTime <= 0)
        {
            return;
        }

        float alpha = Main.getAlpha(renderTime, jump.getMaxTime(), jump.getFadeInTime(), jump.getFadeOutTime());
        int scaledWidth = mc.getMainWindow().getScaledWidth();
        int scaledHeight = mc.getMainWindow().getScaledHeight();
        int xPosition = scaledWidth / 2 - 91;

        mc.getProfiler().startSection("jumpBar");
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
        float f = mc.player.getHorseJumpPower();
        int i = 182;
        int j = (int) (f * 183.0F);
        int k = scaledHeight - TimerUtils.getJumpTranslation();
        gui.blit(matrixStack, xPosition, k, 0, 84, 182, 5);
        if (j > 0)
        {
            gui.blit(matrixStack, xPosition, k, 0, 89, j, 5);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.disableBlend();
        mc.getProfiler().endSection();
    }

    /**
     * Renders the experience bar.
     *
     * @param mc - A Minecraft instance.
     * @param gui - The GUI.
     * @param matrixStack - The rendering transformation matrix stack.
     * @param ticks
     * @param renderTime - How much time before this element becomes fully
     * transparent.
     *
     * @see IngameGui#func_238454_b_(MatrixStack, int)
     */
    public static void renderExperience(Minecraft mc,
                                        IngameGui gui,
                                        MatrixStack matrixStack,
                                        float ticks, double renderTime)
    {
        ConfigManager.TimeValues experience = ConfigManager.getInstance().getHealthTime();
        ConfigManager.TimeValues hotbar = ConfigManager.getInstance().getHotbarTime();
        if (renderTime <= 0)
        {
            return;
        }

        float alpha = Main.getAlpha(renderTime, experience.getMaxTime(), experience.getFadeInTime(), experience.getFadeOutTime());
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);

        if (mc.playerController.gameIsSurvivalOrAdventure())
        {
            int scaledWidth = mc.getMainWindow().getScaledWidth();
            int scaledHeight = mc.getMainWindow().getScaledHeight();
            int x = scaledWidth / 2 - 91;

            mc.getProfiler().startSection("expBar");
            RenderSystem.enableBlend();
            mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
            int i = mc.player.xpBarCap();
            if (i > 0)
            {
                int j = 182;
                int k = (int) (mc.player.experience * 183.0F);
                int l = scaledHeight - TimerUtils.getExperienceTranslation();
                gui.blit(matrixStack, x, l, 0, 64, 182, 5);
                if (k > 0)
                {
                    gui.blit(matrixStack, x, l, 0, 69, k, 5);
                }
            }

            RenderSystem.disableBlend();
            mc.getProfiler().endSection();
            if (mc.player.experienceLevel > 0)
            {
                mc.getProfiler().startSection("expLevel");
                RenderSystem.enableBlend();
                String s = "" + mc.player.experienceLevel;
                int i1 = (scaledWidth - gui.getFontRenderer()
                        .getStringWidth(s)) / 2;
                int j1 = scaledHeight - (int) ((22F * Main.getAlpha(TimerUtils.hotbarTime, hotbar.getMaxTime(), hotbar.getFadeInTime(), hotbar.getFadeOutTime()) + 9F) * alpha) - (int) (4F * alpha);
                gui.getFontRenderer()
                        .drawString(matrixStack,
                                s,
                                (float) (i1 + 1),
                                (float) j1,
                                0);
                gui.getFontRenderer()
                        .drawString(matrixStack,
                                s,
                                (float) (i1 - 1),
                                (float) j1,
                                0);
                gui.getFontRenderer()
                        .drawString(matrixStack,
                                s,
                                (float) i1,
                                (float) (j1 + 1),
                                0);
                gui.getFontRenderer()
                        .drawString(matrixStack,
                                s,
                                (float) i1,
                                (float) (j1 - 1),
                                0);
                gui.getFontRenderer()
                        .drawString(matrixStack,
                                s,
                                (float) i1,
                                (float) j1,
                                8453920);
                RenderSystem.disableBlend();
                mc.getProfiler().endSection();
            }
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }


    /**
     * Renders the hotbar and all the items in it.
     *
     * @param mc - A Minecraft instance.
     * @param gui - The GUI.
     * @param matrixStack - The rendering transformation matrix stack.
     * @param partialTicks
     * @param renderTime - How much time before this element becomes fully
     * transparent.
     *
     * @see IngameGui#renderHotbar(float, MatrixStack)
     */
    public static void renderHotbar(Minecraft mc,
                                    IngameGui gui,
                                    MatrixStack matrixStack,
                                    float partialTicks,
                                    double renderTime)
    {
        ConfigManager.TimeValues hotbar = ConfigManager.getInstance().getHotbarTime();
        final ResourceLocation WIDGETS_TEX_PATH =
                new ResourceLocation("textures/gui/widgets.png");
        PlayerEntity playerentity = mc.player;

        if (renderTime <= 0)
        {
            return;
        }

        float alpha = Main.getAlpha(renderTime, hotbar.getMaxTime(), hotbar.getFadeInTime(), hotbar.getFadeOutTime());

        int scaledWidth = mc.getMainWindow().getScaledWidth();
        int scaledHeight = mc.getMainWindow().getScaledHeight();
        if (playerentity != null)
        {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);
            ItemStack itemstack = playerentity.getHeldItemOffhand();
            HandSide handside = playerentity.getPrimaryHand().opposite();
            int i = scaledWidth / 2;
            int j = gui.getBlitOffset();
            int k = 182;
            int l = 91;
            gui.setBlitOffset(-90);
            gui.blit(matrixStack, i - 91, scaledHeight - TimerUtils.getHotbarTranslation(), 0, 0, 182, 22);
            gui.blit(matrixStack,
                    i - 91 - 1 + playerentity.inventory.currentItem * 20,
                    scaledHeight - (int) (22F * alpha) - 1,
                    0,
                    22,
                    24,
                    22);
            if (!itemstack.isEmpty())
            {
                if (handside == HandSide.LEFT)
                {
                    gui.blit(matrixStack,
                            i - 91 - 29,
                            scaledHeight - (int) (23F * alpha),
                            24,
                            22,
                            29,
                            24);
                }
                else
                {
                    gui.blit(matrixStack,
                            i + 91,
                            scaledHeight - (int) (23F * alpha),
                            53,
                            22,
                            29,
                            24);
                }
            }

            gui.setBlitOffset(j);
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            for (int i1 = 0; i1 < 9; ++i1)
            {
                int j1 = i - 90 + i1 * 20 + 2;
                int k1 = scaledHeight - (int) (16F * alpha) - 3;
                renderHotbarItem(mc, alpha, j1,
                        k1,
                        partialTicks,
                        playerentity,
                        playerentity.inventory.mainInventory.get(i1));
            }

            if (!itemstack.isEmpty())
            {
                int i2 = scaledHeight - (int) (16F * alpha) - 3;
                if (handside == HandSide.LEFT)
                {
                    renderHotbarItem(mc, alpha, i - 91 - 26,
                            i2,
                            partialTicks,
                            playerentity,
                            itemstack);
                }
                else
                {
                    renderHotbarItem(mc, alpha, i + 91 + 10,
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
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                    gui.blit(matrixStack, k2, j2, 0, 94, 18, 18);
                    gui.blit(matrixStack,
                            k2,
                            j2 + 18 - l1,
                            18,
                            112 - l1,
                            18,
                            l1);
                }
            }

            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
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
            float f = (float) stack.getAnimationsToGo() - partialTicks;
            RenderSystem.pushMatrix();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            if (f > 0.0F)
            {
                RenderSystem.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                RenderSystem.translatef((float) (x + 8),
                        (float) (y + 12),
                        0.0F);
                RenderSystem.scalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                RenderSystem.translatef((float) (-(x + 8)),
                        (float) (-(y + 12)),
                        0.0F);
            }

            renderItemAndEffectIntoGUI(mc, alpha, player, stack, x, y);
            if (f > 0.0F)
            {
                RenderSystem.popMatrix();
            }
            RenderSystem.popMatrix();

            mc.getItemRenderer()
                    .renderItemOverlays(mc.fontRenderer, stack, x, y);
        }
    }

    /**
     * Renders a single item on the HUD.
     *
     * @param mc - A Minecraft instance.
     * @param alpha - How transparent the item should be, on a scale from 0 to
     * 1.
     * @param ent - The player that the item is from.
     * @param stack - The item to render.
     * @param x - The x position of the item on the screen.
     * @param y - The y position of the item on the screen
     *
     * @see net.minecraft.client.renderer.ItemRenderer#renderItemIntoGUI(LivingEntity,
     * ItemStack, int, int)
     */
    static void renderItemAndEffectIntoGUI(Minecraft mc,
                                           float alpha,
                                           PlayerEntity ent,
                                           ItemStack stack,
                                           int x,
                                           int y)
    {
        TextureManager textureManager = mc.textureManager;
        IBakedModel bakedmodel = mc.getItemRenderer()
                .getItemModelWithOverrides(stack,
                        (World) null,
                        (LivingEntity) null);
        if (!stack.isEmpty())
        {
            try
            {
                RenderSystem.pushMatrix();
                textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                        .setBlurMipmapDirect(false, false);
                RenderSystem.enableRescaleNormal();
//                RenderSystem.enableAlphaTest();
//                RenderSystem.defaultAlphaFunc();
//                RenderSystem.enableBlend();
//                RenderSystem.blendFunc(GlStateManager.SourceFactor
//                .SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.translatef((float) x, (float) y, 100.0F + 50F);
                RenderSystem.translatef(8.0F, 8.0F, 0.0F);
                RenderSystem.scalef(1.0F, -1.0F, 1.0F);
                RenderSystem.scalef(16.0F, 16.0F, 16.0F);
                MatrixStack matrixstack = new MatrixStack();
                IRenderTypeBuffer.Impl irendertypebuffer$impl =
                        Minecraft.getInstance()
                                .getRenderTypeBuffers()
                                .getBufferSource();
                boolean flag = !bakedmodel.isSideLit();
                if (flag)
                {
                    RenderHelper.setupGuiFlatDiffuseLighting();
                }

                mc.getItemRenderer()
                        .renderItem(stack,
                                ItemCameraTransforms.TransformType.GUI,
                                false,
                                matrixstack,
                                irendertypebuffer$impl,
                                15728880,
                                OverlayTexture.NO_OVERLAY,
                                bakedmodel);
                irendertypebuffer$impl.finish();
                RenderSystem.enableDepthTest();
                if (flag)
                {
                    RenderHelper.setupGui3DDiffuseLighting();
                }

//                RenderSystem.disableAlphaTest();
                RenderSystem.disableRescaleNormal();
                RenderSystem.popMatrix();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable,
                        "Rendering item");
                CrashReportCategory crashreportcategory =
                        crashreport.makeCategory("Item being rendered");
                crashreportcategory.addDetail("Item Type", () -> {
                    return String.valueOf((Object) stack.getItem());
                });
                crashreportcategory.addDetail("Registry Name",
                        () -> String.valueOf(stack.getItem()
                                .getRegistryName()));
                crashreportcategory.addDetail("Item Damage", () -> {
                    return String.valueOf(stack.getDamage());
                });
                crashreportcategory.addDetail("Item NBT", () -> {
                    return String.valueOf((Object) stack.getTag());
                });
                crashreportcategory.addDetail("Item Foil", () -> {
                    return String.valueOf(stack.hasEffect());
                });
                throw new ReportedException(crashreport);
            }
        }
    }
}
