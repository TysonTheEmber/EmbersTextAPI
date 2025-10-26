package net.tysontheember.emberstextapi.core.style;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Captures the identifier and parameters for a glyph-level effect.
 */
public final class GlyphEffect {
    private final ResourceLocation id;
    private final CompoundTag parameters;

    public GlyphEffect(ResourceLocation id) {
        this(id, new CompoundTag());
    }

    public GlyphEffect(ResourceLocation id, CompoundTag parameters) {
        this.id = Objects.requireNonNull(id, "id");
        this.parameters = parameters == null ? new CompoundTag() : parameters.copy();
    }

    public ResourceLocation id() {
        return id;
    }

    public CompoundTag parameters() {
        return parameters.copy();
    }

    public GlyphEffect withParameters(CompoundTag parameters) {
        return new GlyphEffect(id, parameters);
    }

    public GlyphEffect copy() {
        return new GlyphEffect(id, parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GlyphEffect that)) {
            return false;
        }
        return id.equals(that.id) && parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parameters);
    }

    @Override
    public String toString() {
        return "GlyphEffect{" +
                "id=" + id +
                ", parameters=" + parameters +
                '}';
    }
}
