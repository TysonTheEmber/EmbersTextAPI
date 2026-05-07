package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class ScrollEffect extends BaseEffect {

    private static final float BASE_SPEED = 0.04f;
    private static final int MAX_TILES_PER_CHAR = 50;
    private static final float ALPHA_THRESHOLD = 4f / 255f;

    private final float speed;
    private final float viewportW;
    private final String clipMode;
    private final float fadeWidth;
    private final boolean scrollRight;
    private final float gap;

    private float measuredSpanWidth = -1;
    private float frameFirstX;
    private float frameLastX;
    private float frameLastAdvance;
    private int frameCharCount;

    private float advanceAccum;

    private final Set<EffectSettings> ownSiblings = Collections.newSetFromMap(new IdentityHashMap<>());

    public ScrollEffect(@NotNull Params params) {
        super(params);
        this.speed = ValidationHelper.clamp("scroll", "speed",
                params.getDouble("speed")
                        .or(() -> params.getDouble("f"))
                        .map(Number::floatValue)
                        .orElse(1.0f),
                0.1f, 100f);
        float rawW = params.getDouble("w").map(Number::floatValue).orElse(-1f);
        this.viewportW = rawW > 0 ? ValidationHelper.clamp("scroll", "w", rawW, 1f, 1000f) : -1f;
        String rawClip = params.getString("clip").orElse("fade").toLowerCase();
        this.clipMode = switch (rawClip) {
            case "cut", "both" -> rawClip;
            default -> "fade";
        };
        this.fadeWidth = ValidationHelper.clamp("scroll", "fw",
                params.getDouble("fw").map(Number::floatValue).orElse(8.0f),
                1f, 50f);
        this.scrollRight = "right".equalsIgnoreCase(params.getString("dir").orElse("left"));
        this.gap = ValidationHelper.clamp("scroll", "gap",
                params.getDouble("gap").map(Number::floatValue).orElse(0f),
                0f, 500f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        if (ownSiblings.remove(settings)) {
            return;
        }

        if (settings.isShadow) {
            if (settings.index == 0) {
                advanceAccum = 0;
            }
            advanceAccum += settings.charAdvance;

            if (measuredSpanWidth <= 0) {
                settings.a = 0;
                return;
            }
        } else {
            if (frameCharCount > 0 && settings.x < frameLastX) {
                measuredSpanWidth = frameLastX - frameFirstX + frameLastAdvance;
                frameCharCount = 0;
            }

            if (frameCharCount == 0) {
                frameFirstX = settings.x;
                if (measuredSpanWidth <= 0 && advanceAccum > 0) {
                    measuredSpanWidth = advanceAccum;
                }
            }
            frameLastX = settings.x;
            frameLastAdvance = settings.charAdvance;
            frameCharCount++;
        }

        if (measuredSpanWidth <= 0) {
            settings.a = 0;
            return;
        }

        float spanW = measuredSpanWidth;
        float vw = viewportW > 0 ? viewportW : spanW;
        float period = spanW + gap;

        long now = Util.getMillis();
        double scrollOffset = now * (double) BASE_SPEED * speed;
        if (scrollRight) {
            scrollOffset = -scrollOffset;
        }

        float spanStartX = frameFirstX;
        if (settings.isShadow) {
            spanStartX += settings.shadowOffset;
        }

        float viewportStartX = spanStartX + (spanW - vw) / 2f;

        float charRelPos = settings.x - spanStartX;
        float tilePos = (float) (((charRelPos - scrollOffset) % period + period) % period);

        if (tilePos >= spanW) {
            settings.a = 0;
            return;
        }

        float charW = settings.charAdvance;
        float p = tilePos;
        while (p > -charW) {
            p -= period;
        }
        p += period;

        float origA = settings.a;
        boolean mainSet = false;
        int tileCount = 0;

        while (p < vw + charW && tileCount < MAX_TILES_PER_CHAR) {
            float finalAlpha = origA * computeClipAlpha(p, vw);
            if (finalAlpha >= ALPHA_THRESHOLD) {
                if (!mainSet) {
                    settings.x = viewportStartX + p;
                    settings.a = finalAlpha;
                    mainSet = true;
                } else {
                    EffectSettings sib = settings.copy();
                    sib.x = viewportStartX + p;
                    sib.a = finalAlpha;
                    ownSiblings.add(sib);
                    settings.addSibling(sib);
                }
            }
            p += period;
            tileCount++;
        }

        if (!mainSet) {
            settings.a = 0;
        }
    }

    private float computeClipAlpha(float pos, float vw) {
        return switch (clipMode) {
            case "cut" -> (pos >= 0 && pos < vw) ? 1.0f : 0.0f;
            case "both" -> {
                if (pos < 0 || pos >= vw) {
                    yield 0.0f;
                }
                float a = 1.0f;
                if (pos < fadeWidth) {
                    a = pos / fadeWidth;
                }
                if (pos > vw - fadeWidth) {
                    a = Math.min(a, (vw - pos) / fadeWidth);
                }
                yield Math.max(0f, a);
            }
            default -> {
                float a = 1.0f;
                if (pos < fadeWidth) {
                    a = Math.max(0f, pos / fadeWidth);
                }
                if (pos > vw - fadeWidth) {
                    a = Math.min(a, Math.max(0f, (vw - pos) / fadeWidth));
                }
                yield a;
            }
        };
    }

    @NotNull
    @Override
    public String getName() {
        return "scroll";
    }
}
