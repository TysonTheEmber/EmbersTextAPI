/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.gui.overlay;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.scores.Objective;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge wrapper around {@link Gui} to be able to render {@link IGuiOverlay HUD overlays}.
 */
public class ForgeGui extends Gui
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int WHITE = 0xFFFFFF;

    /*
     * If the Euclidean distance to the moused-over block in meters is less than this value, the "Looking at" text will appear on the debug overlay.
     */
    public static double rayTraceDistance = 20.0D;

    public int leftHeight = 39;
    public int rightHeight = 39;

    private Font font = null;

    private final ForgeDebugScreenOverlay debugOverlay;

    public ForgeGui(Minecraft mc)
    {
        super(mc, mc.m_91291_());
        debugOverlay = new ForgeDebugScreenOverlay(mc);
    }

    public Minecraft getMinecraft()
    {
        return f_92986_;
    }

    public void setupOverlayRenderState(boolean blend, boolean depthTest)
    {
        if (blend)
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
        else
        {
            RenderSystem.disableBlend();
        }

        if (depthTest)
        {
            RenderSystem.enableDepthTest();
        }
        else
        {
            RenderSystem.disableDepthTest();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::m_172817_);
    }

    @Override
    public void m_280421_(GuiGraphics guiGraphics, float partialTick)
    {
        this.f_92977_ = this.f_92986_.m_91268_().m_85445_();
        this.f_92978_ = this.f_92986_.m_91268_().m_85446_();

        rightHeight = 39;
        leftHeight = 39;

        if (MinecraftForge.EVENT_BUS.post(new RenderGuiEvent.Pre(f_92986_.m_91268_(), guiGraphics, partialTick)))
        {
            return;
        }

        font = f_92986_.f_91062_;

        this.f_92985_.m_188584_(f_92989_ * 312871L);

        GuiOverlayManager.getOverlays().forEach(entry -> {
            try
            {
                IGuiOverlay overlay = entry.overlay();
                if (pre(entry, guiGraphics)) return;
                overlay.render(this, guiGraphics, partialTick, f_92977_, f_92978_);
                post(entry, guiGraphics);
            } catch (Exception e)
            {
                LOGGER.error("Error rendering overlay '{}'", entry.id(), e);
            }
        });

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        MinecraftForge.EVENT_BUS.post(new RenderGuiEvent.Post(f_92986_.m_91268_(), guiGraphics, partialTick));
    }

    public boolean shouldDrawSurvivalElements()
    {
        return f_92986_.f_91072_.m_105205_() && f_92986_.m_91288_() instanceof Player;
    }

    protected void renderSubtitles(GuiGraphics guiGraphics)
    {
        this.f_92996_.m_280227_(guiGraphics);
    }

    protected void renderBossHealth(GuiGraphics guiGraphics)
    {
        RenderSystem.defaultBlendFunc();
        f_92986_.m_91307_().m_6180_("bossHealth");
        this.f_92999_.m_280652_(guiGraphics);
        f_92986_.m_91307_().m_7238_();
    }

    void renderSpyglassOverlay(GuiGraphics guiGraphics)
    {
        float deltaFrame = this.f_92986_.m_91297_();
        this.f_168664_ = Mth.m_14179_(0.5F * deltaFrame, this.f_168664_, 1.125F);
        if (this.f_92986_.f_91066_.m_92176_().m_90612_())
        {
            if (this.f_92986_.f_91074_.m_150108_())
            {
                this.m_280278_(guiGraphics, this.f_168664_);
            }
            else
            {
                this.f_168664_ = 0.5F;
            }
        }
    }

    void renderHelmet(float partialTick, GuiGraphics guiGraphics)
    {
        ItemStack itemstack = this.f_92986_.f_91074_.m_150109_().m_36052_(3);

        if (this.f_92986_.f_91066_.m_92176_().m_90612_() && !itemstack.m_41619_())
        {
            Item item = itemstack.m_41720_();
            if (item == Blocks.f_50143_.m_5456_())
            {
                m_280155_(guiGraphics, f_92983_, 1.0F);
            }
            else
            {
                IClientItemExtensions.of(item).renderHelmetOverlay(itemstack, f_92986_.f_91074_, this.f_92977_, this.f_92978_, partialTick);
            }
        }
    }

    void renderFrostbite(GuiGraphics guiGraphics)
    {
        if (this.f_92986_.f_91074_.m_146888_() > 0)
        {
            this.m_280155_(guiGraphics, f_168666_, this.f_92986_.f_91074_.m_146889_());
        }
    }

    protected void renderArmor(GuiGraphics guiGraphics, int width, int height)
    {
        f_92986_.m_91307_().m_6180_("armor");

        RenderSystem.enableBlend();
        int left = width / 2 - 91;
        int top = height - leftHeight;

        int level = f_92986_.f_91074_.m_21230_();
        for (int i = 1; level > 0 && i < 20; i += 2)
        {
            if (i < level)
            {
                guiGraphics.m_280218_(f_279580_, left, top, 34, 9, 9, 9);
            }
            else if (i == level)
            {
                guiGraphics.m_280218_(f_279580_, left, top, 25, 9, 9, 9);
            }
            else if (i > level)
            {
                guiGraphics.m_280218_(f_279580_, left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        leftHeight += 10;

        RenderSystem.disableBlend();
        f_92986_.m_91307_().m_7238_();
    }

    @Override
    protected void m_280379_(GuiGraphics guiGraphics, float alpha)
    {
        if (alpha > 0.0F)
        {
            super.m_280379_(guiGraphics, alpha);
        }
    }

    protected void renderAir(int width, int height, GuiGraphics guiGraphics)
    {
        f_92986_.m_91307_().m_6180_("air");
        Player player = (Player) this.f_92986_.m_91288_();
        RenderSystem.enableBlend();
        int left = width / 2 + 91;
        int top = height - rightHeight;

        int air = player.m_20146_();
        if (player.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) || air < 300)
        {
            int full = Mth.m_14165_((double) (air - 2) * 10.0D / 300.0D);
            int partial = Mth.m_14165_((double) air * 10.0D / 300.0D) - full;

            for (int i = 0; i < full + partial; ++i)
            {
                guiGraphics.m_280218_(f_279580_, left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
            }
            rightHeight += 10;
        }

        RenderSystem.disableBlend();
        f_92986_.m_91307_().m_7238_();
    }

    public void renderHealth(int width, int height, GuiGraphics guiGraphics)
    {
        f_92986_.m_91307_().m_6180_("health");
        RenderSystem.enableBlend();

        Player player = (Player) this.f_92986_.m_91288_();
        int health = Mth.m_14167_(player.m_21223_());
        boolean highlight = f_92976_ > (long) f_92989_ && (f_92976_ - (long) f_92989_) / 3L % 2L == 1L;

        if (health < this.f_92973_ && player.f_19802_ > 0)
        {
            this.f_92975_ = Util.m_137550_();
            this.f_92976_ = (long) (this.f_92989_ + 20);
        }
        else if (health > this.f_92973_ && player.f_19802_ > 0)
        {
            this.f_92975_ = Util.m_137550_();
            this.f_92976_ = (long) (this.f_92989_ + 10);
        }

        if (Util.m_137550_() - this.f_92975_ > 1000L)
        {
            this.f_92973_ = health;
            this.f_92974_ = health;
            this.f_92975_ = Util.m_137550_();
        }

        this.f_92973_ = health;
        int healthLast = this.f_92974_;

        AttributeInstance attrMaxHealth = player.m_21051_(Attributes.f_22276_);
        float healthMax = Math.max((float) attrMaxHealth.m_22135_(), Math.max(healthLast, health));
        int absorb = Mth.m_14167_(player.m_6103_());

        int healthRows = Mth.m_14167_((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.f_92985_.m_188584_((long) (f_92989_ * 312871));

        int left = width / 2 - 91;
        int top = height - leftHeight;
        leftHeight += (healthRows * rowHeight);
        if (rowHeight != 10) leftHeight += 10 - rowHeight;

        int regen = -1;
        if (player.m_21023_(MobEffects.f_19605_))
        {
            regen = this.f_92989_ % Mth.m_14167_(healthMax + 5.0F);
        }

        this.m_168688_(guiGraphics, player, left, top, rowHeight, regen, healthMax, health, healthLast, absorb, highlight);

        RenderSystem.disableBlend();
        f_92986_.m_91307_().m_7238_();
    }

    public void renderFood(int width, int height, GuiGraphics guiGraphics)
    {
        f_92986_.m_91307_().m_6180_("food");

        Player player = (Player) this.f_92986_.m_91288_();
        RenderSystem.enableBlend();
        int left = width / 2 + 91;
        int top = height - rightHeight;
        rightHeight += 10;
        boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

        FoodData stats = f_92986_.f_91074_.m_36324_();
        int level = stats.m_38702_();

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            int icon = 16;
            byte background = 0;

            if (f_92986_.f_91074_.m_21023_(MobEffects.f_19612_))
            {
                icon += 36;
                background = 13;
            }
            if (unused) background = 1; //Probably should be a += 1 but vanilla never uses this

            if (player.m_36324_().m_38722_() <= 0.0F && f_92989_ % (level * 3 + 1) == 0)
            {
                y = top + (f_92985_.m_188503_(3) - 1);
            }

            guiGraphics.m_280218_(f_279580_, x, y, 16 + background * 9, 27, 9, 9);

            if (idx < level)
                guiGraphics.m_280218_(f_279580_, x, y, icon + 36, 27, 9, 9);
            else if (idx == level)
                guiGraphics.m_280218_(f_279580_, x, y, icon + 45, 27, 9, 9);
        }
        RenderSystem.disableBlend();
        f_92986_.m_91307_().m_7238_();
    }

    protected void renderSleepFade(int width, int height, GuiGraphics guiGraphics)
    {
        if (f_92986_.f_91074_.m_36318_() > 0)
        {
            f_92986_.m_91307_().m_6180_("sleep");
            int sleepTime = f_92986_.f_91074_.m_36318_();
            float opacity = (float) sleepTime / 100.0F;

            if (opacity > 1.0F)
            {
                opacity = 1.0F - (float) (sleepTime - 100) / 10.0F;
            }

            int color = (int) (220.0F * opacity) << 24 | 1052704;
            guiGraphics.m_285944_(RenderType.m_286086_(), 0, 0, width, height, color);
            f_92986_.m_91307_().m_7238_();
        }
    }

    protected void renderExperience(int x, GuiGraphics guiGraphics)
    {
        guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        if (f_92986_.f_91072_.m_105288_())
        {
            super.m_280276_(guiGraphics, x);
        }
        RenderSystem.enableBlend();
        guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void m_280069_(PlayerRideableJumping playerRideableJumping, GuiGraphics guiGraphics, int x)
    {
        guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        super.m_280069_(playerRideableJumping, guiGraphics, x);

        RenderSystem.enableBlend();
        f_92986_.m_91307_().m_7238_();
        guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void renderHUDText(int width, int height, GuiGraphics guiGraphics)
    {
        f_92986_.m_91307_().m_6180_("forgeHudText");
        RenderSystem.defaultBlendFunc();

        var listL = new ArrayList<String>();
        var listR = new ArrayList<String>();

        if (f_92986_.m_91402_())
        {
            long time = f_92986_.f_91073_.m_46467_();
            if (time >= 120500L)
            {
                listR.add(I18n.m_118938_("demo.demoExpired"));
            }
            else
            {
                listR.add(I18n.m_118938_("demo.remainingTime", StringUtil.m_14404_((int) (120500L - time))));
            }
        }

        if (this.f_92986_.f_91066_.f_92063_)
        {
            debugOverlay.update();
            listL.addAll(debugOverlay.getLeft());
            listR.addAll(debugOverlay.getRight());
        }

        var event = new CustomizeGuiOverlayEvent.DebugText(f_92986_.m_91268_(), guiGraphics, f_92986_.m_91296_(), listL, listR);
        MinecraftForge.EVENT_BUS.post(event);

        int top = 2;
        for (String msg : listL)
        {
            if (msg != null && !msg.isEmpty())
            {
                guiGraphics.m_280509_(1, top - 1, 2 + font.m_92895_(msg) + 1, top + font.f_92710_ - 1, -1873784752);
                guiGraphics.m_280056_(font, msg, 2, top, 14737632, false);
            }
            top += font.f_92710_;
        }

        top = 2;
        for (String msg : listR)
        {
            if (msg != null && !msg.isEmpty())
            {
                int w = font.m_92895_(msg);
                int left = width - 2 - w;
                guiGraphics.m_280509_(left - 1, top - 1, left + w + 1, top + font.f_92710_ - 1, -1873784752);
                guiGraphics.m_280056_(font, msg, left, top, 14737632, false);
            }
            top += font.f_92710_;
        }

        f_92986_.m_91307_().m_7238_();
    }

    protected void renderFPSGraph(GuiGraphics guiGraphics)
    {
        if (this.f_92986_.f_91066_.f_92063_ && this.f_92986_.f_91066_.f_92065_)
        {
            this.debugOverlay.m_94056_(guiGraphics);
        }
    }

    @Override
    public void m_93091_()
    {
        super.m_93091_();
        this.debugOverlay.m_94040_();
    }

    protected void renderRecordOverlay(int width, int height, float partialTick, GuiGraphics guiGraphics)
    {
        if (f_92991_ > 0)
        {
            f_92986_.m_91307_().m_6180_("overlayMessage");
            float hue = (float) f_92991_ - partialTick;
            int opacity = (int) (hue * 255.0F / 20.0F);
            if (opacity > 255) opacity = 255;

            if (opacity > 8)
            {
                //Include a shift based on the bar height plus the difference between the height that renderSelectedItemName
                // renders at (59) and the height that the overlay/status bar renders at (68) by default
                int yShift = Math.max(leftHeight, rightHeight) + (68 - 59);
                guiGraphics.m_280168_().m_85836_();
                //If y shift is smaller less than the default y level, just render it at the base y level
                guiGraphics.m_280168_().m_85837_(width / 2D, height - Math.max(yShift, 68), 0.0D);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                int color = (f_92992_ ? Mth.m_14169_(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE);
                int messageWidth = font.m_92852_(f_92990_);
                m_93039_(guiGraphics, font, -4, messageWidth, 16777215 | (opacity << 24));
                guiGraphics.m_280648_(font, f_92990_.m_7532_(), -messageWidth / 2, -4, color | (opacity << 24));
                RenderSystem.disableBlend();
                guiGraphics.m_280168_().m_85849_();
            }

            f_92986_.m_91307_().m_7238_();
        }
    }

    protected void renderTitle(int width, int height, float partialTick, GuiGraphics guiGraphics)
    {
        if (f_93001_ != null && f_93000_ > 0)
        {
            f_92986_.m_91307_().m_6180_("titleAndSubtitle");
            float age = (float) this.f_93000_ - partialTick;
            int opacity = 255;

            if (f_93000_ > f_92972_ + f_92971_)
            {
                float f3 = (float) (f_92970_ + f_92971_ + f_92972_) - age;
                opacity = (int) (f3 * 255.0F / (float) f_92970_);
            }
            if (f_93000_ <= f_92972_) opacity = (int) (age * 255.0F / (float) this.f_92972_);

            opacity = Mth.m_14045_(opacity, 0, 255);

            if (opacity > 8)
            {
                guiGraphics.m_280168_().m_85836_();
                guiGraphics.m_280168_().m_85837_(width / 2D, height / 2D, 0.0D);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                guiGraphics.m_280168_().m_85836_();
                guiGraphics.m_280168_().m_85841_(4.0F, 4.0F, 4.0F);
                int l = opacity << 24 & -16777216;
                guiGraphics.m_280649_(this.font, this.f_93001_.m_7532_(), -this.m_93082_().m_92852_(this.f_93001_) / 2, -10, 16777215 | l, true);
                guiGraphics.m_280168_().m_85849_();
                if (this.f_93002_ != null)
                {
                    guiGraphics.m_280168_().m_85836_();
                    guiGraphics.m_280168_().m_85841_(2.0F, 2.0F, 2.0F);
                    guiGraphics.m_280649_(this.font, this.f_93002_.m_7532_(), -this.m_93082_().m_92852_(this.f_93002_) / 2, 5, 16777215 | l, true);
                    guiGraphics.m_280168_().m_85849_();
                }
                RenderSystem.disableBlend();
                guiGraphics.m_280168_().m_85849_();
            }

            this.f_92986_.m_91307_().m_7238_();
        }
    }

    protected void renderChat(int width, int height, GuiGraphics guiGraphics)
    {
        f_92986_.m_91307_().m_6180_("chat");

        Window window = f_92986_.m_91268_();
        var event = new CustomizeGuiOverlayEvent.Chat(window, guiGraphics, f_92986_.m_91296_(), 0, height - 40);
        MinecraftForge.EVENT_BUS.post(event);

        guiGraphics.m_280168_().m_85836_();
        // We give the absolute Y position of the chat component in the event and account for the chat component's own offsetting here.
        guiGraphics.m_280168_().m_85837_(event.getPosX(), (event.getPosY() - height + 40) / f_92988_.m_93815_(), 0.0D);
        int mouseX = Mth.m_14107_(f_92986_.f_91067_.m_91589_() * window.m_85445_() / window.m_85443_());
        int mouseY = Mth.m_14107_(f_92986_.f_91067_.m_91594_() * window.m_85446_() / window.m_85444_());
        f_92988_.m_280165_(guiGraphics, f_92989_, mouseX, mouseY);
        guiGraphics.m_280168_().m_85849_();

        f_92986_.m_91307_().m_7238_();
    }

    protected void renderPlayerList(int width, int height, GuiGraphics guiGraphics)
    {
        Objective scoreobjective = this.f_92986_.f_91073_.m_6188_().m_83416_(0);
        ClientPacketListener handler = f_92986_.f_91074_.f_108617_;

        if (f_92986_.f_91066_.f_92099_.m_90857_() && (!f_92986_.m_91090_() || handler.m_105142_().size() > 1 || scoreobjective != null))
        {
            this.f_92998_.m_94556_(true);
            this.f_92998_.m_280406_(guiGraphics, width, this.f_92986_.f_91073_.m_6188_(), scoreobjective);

        }
        else
        {
            this.f_92998_.m_94556_(false);
        }
    }

    protected void renderHealthMount(int width, int height, GuiGraphics guiGraphics)
    {
        Player player = (Player) f_92986_.m_91288_();
        Entity tmp = player.m_20202_();
        if (!(tmp instanceof LivingEntity)) return;

        boolean unused = false;
        int left_align = width / 2 + 91;

        f_92986_.m_91307_().m_6182_("mountHealth");
        RenderSystem.enableBlend();
        LivingEntity mount = (LivingEntity) tmp;
        int health = (int) Math.ceil((double) mount.m_21223_());
        float healthMax = mount.m_21233_();
        int hearts = (int) (healthMax + 0.5F) / 2;

        if (hearts > 30) hearts = 30;

        final int MARGIN = 52;
        final int BACKGROUND = MARGIN + (unused ? 1 : 0);
        final int HALF = MARGIN + 45;
        final int FULL = MARGIN + 36;

        for (int heart = 0; hearts > 0; heart += 20)
        {
            int top = height - rightHeight;

            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;

            for (int i = 0; i < rowCount; ++i)
            {
                int x = left_align - i * 8 - 9;
                guiGraphics.m_280218_(f_279580_, x, top, BACKGROUND, 9, 9, 9);

                if (i * 2 + 1 + heart < health)
                    guiGraphics.m_280218_(f_279580_, x, top, FULL, 9, 9, 9);
                else if (i * 2 + 1 + heart == health)
                    guiGraphics.m_280218_(f_279580_, x, top, HALF, 9, 9, 9);
            }

            rightHeight += 10;
        }
        RenderSystem.disableBlend();
    }

    //Helper macros
    private boolean pre(NamedGuiOverlay overlay, GuiGraphics guiGraphics)
    {
        return MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Pre(f_92986_.m_91268_(), guiGraphics, f_92986_.m_91296_(), overlay));
    }

    private void post(NamedGuiOverlay overlay, GuiGraphics guiGraphics)
    {
        MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Post(f_92986_.m_91268_(), guiGraphics, f_92986_.m_91296_(), overlay));
    }

    private static class ForgeDebugScreenOverlay extends DebugScreenOverlay
    {
        private final Minecraft mc;

        private ForgeDebugScreenOverlay(Minecraft mc)
        {
            super(mc);
            this.mc = mc;
        }

        public void update()
        {
            Entity entity = this.mc.m_91288_();
            this.f_94032_ = entity.m_19907_(rayTraceDistance, 0.0F, false);
            this.f_94033_ = entity.m_19907_(rayTraceDistance, 0.0F, true);
        }

        @Override
        protected void m_280186_(GuiGraphics guiGraphics)
        {
            // Replicate the depth test state "leak" caused by the text that is rendered here in vanilla
            // being flushed when the graphs start drawing (PR #9539)
            RenderSystem.disableDepthTest();
        }

        @Override
        protected void m_280532_(GuiGraphics guiGraphics) {}

        private List<String> getLeft()
        {
            List<String> ret = this.m_94075_();
            ret.add("");
            boolean flag = this.mc.m_91092_() != null;
            ret.add("Debug: Pie [shift]: " + (this.mc.f_91066_.f_92064_ ? "visible" : "hidden") + (flag ? " FPS + TPS" : " FPS") + " [alt]: " + (this.mc.f_91066_.f_92065_ ? "visible" : "hidden"));
            ret.add("For help: press F3 + Q");
            return ret;
        }

        private List<String> getRight()
        {
            return this.m_94078_();
        }
    }
}
