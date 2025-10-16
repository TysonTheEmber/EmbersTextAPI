package net.tysontheember.emberstextapi.text;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * High level renderer capable of drawing {@link AttributedText} instances to a
 * {@link GuiGraphics} surface while applying registered {@link TextEffect}s.
 */
public final class EmbersText {
    private EmbersText() {
    }

    public static void draw(GuiGraphics graphics, float x, float y, int maxWidth, AttributedText text, DrawStyle style) {
        Objects.requireNonNull(graphics, "graphics");
        if (text == null) {
            return;
        }
        if (style == null) {
            style = DrawStyle.defaultStyle();
        }
        Font font = Minecraft.getInstance().font;
        List<SpanEffects> compiled = compileSpans(text, style);
        float baseX = x;
        float cursorX = x;
        float cursorY = y;
        float now = TextAnimationClock.now();
        int lineHeight = font.lineHeight;

        for (int glyphIndex = 0; glyphIndex < text.length(); glyphIndex++) {
            char ch = text.text().charAt(glyphIndex);
            if (ch == '\n') {
                cursorX = baseX;
                cursorY += lineHeight * style.lineHeightMultiplier();
                continue;
            }

            SpanEffects spanEffects = findSpan(compiled, glyphIndex);
            Span span = spanEffects != null ? spanEffects.span() : null;
            int spanIndex = spanEffects != null ? spanEffects.index() : -1;
            int spanLength = span != null ? Math.max(1, span.end() - span.start()) : 1;
            int spanLocal = span != null ? glyphIndex - span.start() : 0;
            List<TextEffect> effects = spanEffects != null ? spanEffects.effects() : List.of();

            TextEffect.GlyphState state = new TextEffect.GlyphState(style.baseColor(), style.shadow());
            TextEffect.GlyphContext context = new TextEffect.GlyphContext(
                    glyphIndex,
                    spanIndex,
                    spanLocal,
                    spanLength,
                    ch,
                    now,
                    style.seed(),
                    text,
                    span
            );
            for (TextEffect effect : effects) {
                effect.apply(context, state);
            }
            if (!state.visible()) {
                continue;
            }

            float scale = style.scale() * state.scale();
            float drawX = cursorX + state.offsetX();
            float drawY = cursorY + state.offsetY();
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(drawX, drawY, 0);
            pose.scale(scale, scale, 1f);

            Style glyphStyle = Style.EMPTY.withBold(state.bold()).withItalic(state.italic());
            Component component = Component.literal(String.valueOf(ch)).setStyle(glyphStyle);

            int color = state.resolvedColor();
            if (state.shadow()) {
                int alpha = Math.round(((color >> 24) & 0xFF) * state.shadowAlpha());
                int shadowColor = (alpha << 24);
                graphics.drawString(font, component, Math.round(state.shadowOffsetX()), Math.round(state.shadowOffsetY()), shadowColor, false);
            }
            graphics.drawString(font, component, 0, 0, color, false);
            pose.popPose();

            float advance = font.width(component) + style.letterSpacing();
            cursorX += advance * scale;
            if (maxWidth > 0 && cursorX - baseX > maxWidth) {
                cursorX = baseX;
                cursorY += lineHeight * style.lineHeightMultiplier();
            }
        }
    }

    private static List<SpanEffects> compileSpans(AttributedText text, DrawStyle style) {
        List<Span> spans = text.spans();
        if (spans.isEmpty()) {
            return List.of();
        }
        List<SpanEffects> result = new ArrayList<>(spans.size());
        for (int i = 0; i < spans.size(); i++) {
            Span span = spans.get(i);
            TextEffect.CompileContext context = new TextEffect.CompileContext(text, span, style, style.warningSink());
            List<TextEffect> effects = Effects.compileAll(span.attributes(), context);
            result.add(new SpanEffects(span, effects, i));
        }
        return result;
    }

    private static SpanEffects findSpan(List<SpanEffects> spans, int index) {
        for (SpanEffects span : spans) {
            if (index >= span.span().start() && index < span.span().end()) {
                return span;
            }
        }
        return null;
    }

    private record SpanEffects(Span span, List<TextEffect> effects, int index) {
    }
}
