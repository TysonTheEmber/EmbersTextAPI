package net.tysontheember.emberstextapi.core.style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Holds all extra per-span styling metadata that vanilla {@link net.minecraft.network.chat.Style} normally lacks.
 */
public class SpanEffectState {
    private SpanGradient gradient;
    private TypewriterState typewriter;
    private final List<ShakeState> shakes = new ArrayList<>();
    private final List<GlyphEffect> glyphEffects = new ArrayList<>();
    private final List<InlineAttachment> attachments = new ArrayList<>();
    private final Map<ResourceLocation, CompoundTag> customData = new LinkedHashMap<>();

    public SpanEffectState() {
    }

    public SpanEffectState(SpanEffectState other) {
        this.gradient = other.gradient != null ? other.gradient.copy() : null;
        this.typewriter = other.typewriter;
        other.shakes.forEach(shake -> this.shakes.add(shake));
        other.glyphEffects.forEach(effect -> this.glyphEffects.add(effect.copy()));
        other.attachments.forEach(attachment -> this.attachments.add(attachment.copy()));
        other.customData.forEach((key, value) -> this.customData.put(key, value.copy()));
    }

    public SpanGradient gradient() {
        return gradient;
    }

    public void setGradient(SpanGradient gradient) {
        this.gradient = gradient;
    }

    public TypewriterState typewriter() {
        return typewriter;
    }

    public void setTypewriter(TypewriterState typewriter) {
        this.typewriter = typewriter;
    }

    public List<ShakeState> shakes() {
        return shakes;
    }

    public void setShakes(Collection<ShakeState> values) {
        this.shakes.clear();
        if (values != null) {
            this.shakes.addAll(values);
        }
    }

    public List<GlyphEffect> glyphEffects() {
        return glyphEffects;
    }

    public void setGlyphEffects(Collection<GlyphEffect> values) {
        this.glyphEffects.clear();
        if (values != null) {
            values.forEach(effect -> this.glyphEffects.add(effect.copy()));
        }
    }

    public List<InlineAttachment> attachments() {
        return attachments;
    }

    public void setAttachments(Collection<InlineAttachment> values) {
        this.attachments.clear();
        if (values != null) {
            values.forEach(attachment -> this.attachments.add(attachment.copy()));
        }
    }

    public Map<ResourceLocation, CompoundTag> customData() {
        return customData;
    }

    public void putCustomData(ResourceLocation key, CompoundTag value) {
        if (key == null) {
            return;
        }
        this.customData.put(key, value == null ? new CompoundTag() : value.copy());
    }

    public void putAllCustomData(Map<ResourceLocation, CompoundTag> values) {
        if (values == null) {
            return;
        }
        values.forEach((key, value) -> putCustomData(key, value));
    }

    public boolean isEmpty() {
        return gradient == null && typewriter == null && shakes.isEmpty() && glyphEffects.isEmpty()
                && attachments.isEmpty() && customData.isEmpty();
    }

    public SpanEffectState copy() {
        return new SpanEffectState(this);
    }

    public void appendFrom(SpanEffectState other) {
        if (other == null) {
            return;
        }
        if (other.gradient != null) {
            this.gradient = other.gradient.copy();
        }
        if (other.typewriter != null) {
            this.typewriter = other.typewriter;
        }
        if (!other.shakes.isEmpty()) {
            this.shakes.addAll(other.shakes);
        }
        if (!other.glyphEffects.isEmpty()) {
            other.glyphEffects.forEach(effect -> this.glyphEffects.add(effect.copy()));
        }
        if (!other.attachments.isEmpty()) {
            other.attachments.forEach(attachment -> this.attachments.add(attachment.copy()));
        }
        if (!other.customData.isEmpty()) {
            other.customData.forEach((key, value) -> this.customData.put(key, value.copy()));
        }
    }

    public static SpanEffectState merge(SpanEffectState base, SpanEffectState overlay) {
        boolean baseEmpty = base == null || base.isEmpty();
        boolean overlayEmpty = overlay == null || overlay.isEmpty();
        if (baseEmpty && overlayEmpty) {
            return null;
        }
        SpanEffectState result = baseEmpty ? new SpanEffectState() : base.copy();
        if (!overlayEmpty) {
            result.appendFrom(overlay);
        }
        return result.isEmpty() ? null : result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpanEffectState that)) {
            return false;
        }
        return Objects.equals(gradient, that.gradient)
                && Objects.equals(typewriter, that.typewriter)
                && shakes.equals(that.shakes)
                && glyphEffects.equals(that.glyphEffects)
                && attachments.equals(that.attachments)
                && compareCustomData(that.customData);
    }

    private boolean compareCustomData(Map<ResourceLocation, CompoundTag> other) {
        if (customData.size() != other.size()) {
            return false;
        }
        for (Map.Entry<ResourceLocation, CompoundTag> entry : customData.entrySet()) {
            CompoundTag value = entry.getValue();
            CompoundTag otherValue = other.get(entry.getKey());
            if (otherValue == null || !Objects.equals(value, otherValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradient, typewriter, shakes, glyphEffects, attachments, customData.entrySet());
    }

    @Override
    public String toString() {
        return "SpanEffectState{" +
                "gradient=" + gradient +
                ", typewriter=" + typewriter +
                ", shakes=" + shakes +
                ", glyphEffects=" + glyphEffects +
                ", attachments=" + attachments +
                ", customData=" + customData +
                '}';
    }
}
