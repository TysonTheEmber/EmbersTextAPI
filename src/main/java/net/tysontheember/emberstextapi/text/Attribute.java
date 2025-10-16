package net.tysontheember.emberstextapi.text;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a single text attribute applied to a span within an
 * {@link AttributedText}. Attributes are identified by a
 * {@link ResourceLocation} and may carry arbitrary typed parameters.
 */
public final class Attribute {
    private final ResourceLocation id;
    private final Params params;

    public Attribute(ResourceLocation id, Params params) {
        this.id = Objects.requireNonNull(id, "id");
        this.params = params == null ? Params.of(Map.of()) : params;
    }

    public ResourceLocation id() {
        return id;
    }

    public Params params() {
        return params;
    }

    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        params.toBuffer(buf);
    }

    public static Attribute fromBuffer(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Params params = Params.fromBuffer(buf);
        return new Attribute(id, params);
    }

    @Override
    public String toString() {
        return "Attribute{" + id + ", params=" + params + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute attribute)) return false;
        return id.equals(attribute.id) && params.equals(attribute.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, params);
    }
}
