package net.tysontheember.emberstextapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.tysontheember.emberstextapi.client.text.SpanStyleExtras;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class ActiveMessage {
    private final UUID id;
    private ImmersiveMessage message;

    public ActiveMessage(UUID id, ImmersiveMessage message) {
        this.id = id;
        this.message = message;
    }

    public UUID id() {
        return id;
    }

    public ImmersiveMessage message() {
        return message;
    }

    public void tick() {
        message.tickEffects();
    }

    public boolean isExpired() {
        return message.isFinished();
    }

    public void render(GuiGraphics graphics, float partialTick) {
        Component draw = message.component();
        int colour = message.renderColour(partialTick);
        float scale = message.getTextScale();
        int wrap = message.getWrapWidth();
        String fontKey = message.fontKey();
        String literal = draw.getString();
        String signature = null;
        if (draw.getStyle() instanceof SpanStyleExtras extras) {
            signature = extras.eta$getSpanSignature();
        }
        TextLayoutCache.Key key = new TextLayoutCache.Key(draw, literal, signature, colour, scale, wrap, fontKey);
        TextLayoutCache.Layout layout = TextLayoutCache.getOrCompute(key, () -> message.buildLayout(draw));
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        message.renderWithLayout(graphics, draw, layout, screenW, screenH, partialTick);
    }

    public void update(ImmersiveMessage newMessage) {
        this.message = newMessage;
    }
}
