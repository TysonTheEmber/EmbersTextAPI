package net.tysontheember.emberstextapi.immersivemessages.effects.message;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;

public final class MessageEffectContext {

    public final GuiGraphicsExtractor graphics;
    public final float elapsedSeconds;
    public final float partialTick;
    public final float messageWidth;
    public final float messageHeight;
    public final float currentScale;

    public MessageEffectContext(@NotNull GuiGraphicsExtractor graphics,
                                float elapsedSeconds,
                                float partialTick,
                                float messageWidth,
                                float messageHeight,
                                float currentScale) {
        this.graphics = graphics;
        this.elapsedSeconds = elapsedSeconds;
        this.partialTick = partialTick;
        this.messageWidth = messageWidth;
        this.messageHeight = messageHeight;
        this.currentScale = currentScale;
    }
}
