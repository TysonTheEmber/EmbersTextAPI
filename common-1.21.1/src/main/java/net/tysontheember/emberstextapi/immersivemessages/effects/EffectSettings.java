package net.tysontheember.emberstextapi.immersivemessages.effects;

import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EffectSettings {

    public static final float DEFAULT_SHADOW_OFFSET = 1.0f;
    public static final float DEFAULT_SCALE = 1.0f;
    private static final int SIBLINGS_INITIAL_CAPACITY = 4;

    public float x;
    public float y;
    public float rot;
    public float scale;

    public float r;
    public float g;
    public float b;
    public float a;

    public int index;

    public int absoluteIndex;

    public int codepoint;

    public float charAdvance;

    public boolean useRandomGlyph;

    public boolean isShadow;
    public float shadowOffset;

    public TypewriterTrack typewriterTrack;

    public Object obfuscateKey;
    public Object obfuscateStableKey;
    public net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateTrack obfuscateTrack;
    public int obfuscateSpanStart;
    public int obfuscateSpanLength;

    public int typewriterIndex;

    public List<EffectSettings> siblings;

    public float maskTop;
    public float maskBottom;

    public EffectSettings() {
        this.x = 0f;
        this.y = 0f;
        this.rot = 0f;
        this.scale = DEFAULT_SCALE;
        this.r = 1f;
        this.g = 1f;
        this.b = 1f;
        this.a = 1f;
        this.index = 0;
        this.absoluteIndex = 0;
        this.codepoint = 0;
        this.charAdvance = 0f;
        this.useRandomGlyph = false;
        this.isShadow = false;
        this.shadowOffset = DEFAULT_SHADOW_OFFSET;
        this.typewriterTrack = null;
        this.obfuscateKey = null;
        this.obfuscateStableKey = null;
        this.obfuscateTrack = null;
        this.obfuscateSpanStart = -1;
        this.obfuscateSpanLength = -1;
        this.typewriterIndex = -1;
        this.siblings = null;
        this.maskTop = 0f;
        this.maskBottom = 0f;
    }

    public EffectSettings(float x, float y, float r, float g, float b, float a,
                          int index, int codepoint, boolean isShadow) {
        this.x = x;
        this.y = y;
        this.rot = 0f;
        this.scale = DEFAULT_SCALE;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.index = index;
        this.absoluteIndex = index;
        this.codepoint = codepoint;
        this.charAdvance = 0f;
        this.useRandomGlyph = false;
        this.isShadow = isShadow;
        this.shadowOffset = DEFAULT_SHADOW_OFFSET;
        this.typewriterTrack = null;
        this.obfuscateKey = null;
        this.obfuscateStableKey = null;
        this.obfuscateTrack = null;
        this.obfuscateSpanStart = -1;
        this.obfuscateSpanLength = -1;
        this.typewriterIndex = -1;
        this.siblings = null;
        this.maskTop = 0f;
        this.maskBottom = 0f;
    }

    public List<EffectSettings> getSiblings() {
        if (siblings == null) {
            siblings = new ArrayList<>(SIBLINGS_INITIAL_CAPACITY);
        }
        return siblings;
    }

    public void addSibling(EffectSettings sibling) {
        getSiblings().add(sibling);
    }

    public boolean hasSiblings() {
        return siblings != null && !siblings.isEmpty();
    }

    public List<EffectSettings> getSiblingsOrEmpty() {
        return siblings != null ? siblings : Collections.emptyList();
    }

    public EffectSettings copy() {
        EffectSettings copy = new EffectSettings();
        copy.x = this.x;
        copy.y = this.y;
        copy.rot = this.rot;
        copy.scale = this.scale;
        copy.r = this.r;
        copy.g = this.g;
        copy.b = this.b;
        copy.a = this.a;
        copy.index = this.index;
        copy.absoluteIndex = this.absoluteIndex;
        copy.codepoint = this.codepoint;
        copy.charAdvance = this.charAdvance;
        copy.useRandomGlyph = this.useRandomGlyph;
        copy.isShadow = this.isShadow;
        copy.shadowOffset = this.shadowOffset;
        copy.typewriterTrack = this.typewriterTrack;
        copy.obfuscateKey = this.obfuscateKey;
        copy.obfuscateStableKey = this.obfuscateStableKey;
        copy.obfuscateTrack = this.obfuscateTrack;
        copy.obfuscateSpanStart = this.obfuscateSpanStart;
        copy.obfuscateSpanLength = this.obfuscateSpanLength;
        copy.typewriterIndex = this.typewriterIndex;
        copy.maskTop = this.maskTop;
        copy.maskBottom = this.maskBottom;
        return copy;
    }

    public void reset() {
        this.x = 0f;
        this.y = 0f;
        this.rot = 0f;
        this.scale = DEFAULT_SCALE;
        this.r = 1f;
        this.g = 1f;
        this.b = 1f;
        this.a = 1f;
        if (this.siblings != null) {
            this.siblings.clear();
        }
        this.maskTop = 0f;
        this.maskBottom = 0f;
    }

    public void clampColors() {
        this.r = Math.max(0f, Math.min(1f, this.r));
        this.g = Math.max(0f, Math.min(1f, this.g));
        this.b = Math.max(0f, Math.min(1f, this.b));
        this.a = Math.max(0f, Math.min(1f, this.a));
    }

    public int getPackedColor() {
        int ai = (int) (Math.max(0f, Math.min(1f, a)) * 255);
        int ri = (int) (Math.max(0f, Math.min(1f, r)) * 255);
        int gi = (int) (Math.max(0f, Math.min(1f, g)) * 255);
        int bi = (int) (Math.max(0f, Math.min(1f, b)) * 255);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    @Override
    public String toString() {
        int siblingCount = siblings != null ? siblings.size() : 0;
        return String.format("EffectSettings[pos=(%.1f,%.1f) rot=%.2f scale=%.2f rgba=(%.2f,%.2f,%.2f,%.2f) idx=%d abs=%d cp=%d shadow=%b siblings=%d]",
                x, y, rot, scale, r, g, b, a, index, absoluteIndex, codepoint, isShadow, siblingCount);
    }
}
